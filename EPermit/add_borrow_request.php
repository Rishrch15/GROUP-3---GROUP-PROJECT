<?php
// ====================================================================================
// TEMPORARY DEBUGGING: Enable error reporting (REMOVE IN PRODUCTION!)
// These lines will force PHP to display all errors, warnings, and notices directly
// in the output, which will help you see what's causing the <br> tag.
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
// ====================================================================================

// Set the Content-Type header to application/json. This is crucial for Volley to correctly interpret the response.
// This header MUST be sent BEFORE any other output (like <br>).
header('Content-Type: application/json');

// Initialize a response array to send back to the Android app
$response = array();

// --- Database Credentials (UPDATE THESE!) ---
// These MUST be correct for your MySQL setup.
$servername = "localhost"; // e.g., "localhost" if XAMPP/WAMP is on the same machine
$username = "root";        // Your MySQL username
$password = "";            // Your MySQL password (often empty for XAMPP default 'root' user)
$dbname = "e-permit";   // The database name you created using the SQL above

// Create database connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check database connection
if ($conn->connect_error) {
    // If connection fails, prepare a JSON error response and exit.
    $response["success"] = false;
    $response["message"] = "Database connection failed: " . $conn->connect_error;
    echo json_encode($response);
    exit(); // Stop script execution
}

// Get the raw POST data (JSON string from Android)
$json_data = file_get_contents('php://input');

// ====================================================================================
// TEMPORARY DEBUGGING: Log raw incoming JSON and check for unexpected output before parsing
// This will help confirm what exact string PHP is receiving.
// You can uncomment the line below to write the raw input to a file for inspection.
// error_log("Raw JSON received: " . $json_data); // Logs to Apache/PHP error log
// file_put_contents('php_input_log.txt', date('Y-m-d H:i:s') . " - Raw Input: " . $json_data . "\n", FILE_APPEND);
// ====================================================================================


// Decode the JSON data into an associative PHP array
$data = json_decode($json_data, true);

// Check if JSON decoding was successful and data is not empty
if (json_last_error() !== JSON_ERROR_NONE || empty($data)) {
    $response["success"] = false;
    $response["message"] = "Invalid or empty JSON data received from client: " . json_last_error_msg();
    echo json_encode($response);
    exit();
}

// --- Extract data for the main borrow_requests table ---
// Use null coalescing operator (??) for robustness: if a key doesn't exist, it defaults to an empty string.
$date_submitted = $data['date_submitted'] ?? '';
$department = $data['department'] ?? '';
$borrower_name = $data['borrower_name'] ?? '';
$gender = $data['gender'] ?? '';
$borrower_id = $data['borrower_id'] ?? ''; // This is your deviceId from Android
$project_name = $data['project_name'] ?? '';
$date_of_project = $data['date_of_project'] ?? '';
$time_of_project = $data['time_of_project'] ?? '';
$venue = $data['venue'] ?? '';
$status = $data['status'] ?? 'Pending'; // Default to 'Pending' if not provided by client

// Basic validation for critical fields before insertion
if (empty($date_submitted) || empty($department) || empty($borrower_name) || empty($borrower_id) || empty($project_name) || empty($date_of_project) || empty($time_of_project) || empty($venue)) {
    $response["success"] = false;
    $response["message"] = "One or more main form fields are missing or empty in the received data.";
    echo json_encode($response);
    exit();
}

// Prepare and bind for inserting into `borrow_requests` table
// Using prepared statements for security (prevents SQL injection)
$stmt_main = $conn->prepare("INSERT INTO `borrow_requests` (`date_submitted`, `department`, `borrower_name`, `gender`, `borrower_id`, `project_name`, `date_of_project`, `time_of_project`, `venue`, `status`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

// Check if the prepare statement for main request was successful
if ($stmt_main === false) {
    $response["success"] = false;
    $response["message"] = "Failed to prepare main request statement: " . $conn->error;
    echo json_encode($response);
    exit();
}

// 'ssssssssss' indicates that all 10 parameters are strings.
$stmt_main->bind_param("ssssssssss", $date_submitted, $department, $borrower_name, $gender, $borrower_id, $project_name, $date_of_project, $time_of_project, $venue, $status);

// Execute the main request insertion
if ($stmt_main->execute()) {
    $request_id = $conn->insert_id; // Get the auto-generated ID of the newly inserted request
    $response["success"] = true;
    $response["message"] = "Borrow request submitted successfully!";
    $response["request_id"] = $request_id; // Include the new request ID in the response for Android

    // --- Handle items if they exist in the JSON data ---
    if (isset($data['items']) && is_array($data['items']) && !empty($data['items'])) {
        // Prepare statement for inserting into `borrow_request_items` table
        $stmt_items = $conn->prepare("INSERT INTO `borrow_request_items` (`request_id`, `qty`, `description`, `dateOfTransfer`, `locationFrom`, `locationTo`, `remarks`) VALUES (?, ?, ?, ?, ?, ?, ?)");
        
        // Check if the prepare statement for items was successful
        if ($stmt_items === false) {
            error_log("Failed to prepare items statement for request_id $request_id: " . $conn->error);
            // We've already set success=true, so just log this issue.
            // You might want to adjust logic if item insertion is critical for "success".
        } else {
            // 'issssss' indicates: integer (request_id), then 6 string parameters.
            foreach ($data['items'] as $item) {
                $item_qty = $item['qty'] ?? '';
                $item_description = $item['description'] ?? '';
                $item_date_of_transfer = $item['dateOfTransfer'] ?? '';
                $item_location_from = $item['locationFrom'] ?? '';
                $item_location_to = $item['locationTo'] ?? '';
                $item_remarks = $item['remarks'] ?? '';

                // Bind item parameters to the prepared statement
                $stmt_items->bind_param("issssss", $request_id, $item_qty, $item_description, $item_date_of_transfer, $item_location_from, $item_location_to, $item_remarks);
                
                // Execute item insertion
                if (!$stmt_items->execute()) {
                    // Log item insertion error. This won't stop the main request, but it's good to know.
                    error_log("Error inserting item for request_id $request_id: " . $stmt_items->error);
                    // Optionally, you could add this error to the response message for debugging
                    // $response["message"] .= " (Error with some items: " . $stmt_items->error . ")";
                }
            }
            $stmt_items->close(); // Close the items statement
        }
    } else {
        error_log("No items or invalid items array received for request_id: " . $request_id);
    }

} else {
    // If the main request insertion failed
    $response["success"] = false;
    $response["message"] = "Error submitting main borrow request: " . $stmt_main->error;
}

$stmt_main->close(); // Close the main request statement
$conn->close(); // Close the database connection

// Encode the final response array to JSON and echo it back to the Android app
echo json_encode($response);

// ====================================================================================
// TEMPORARY DEBUGGING: REMOVE THESE LINES AFTER DEBUGGING!
// They should not be in production code.
// ini_set('display_errors', 0);
// ini_set('display_startup_errors', 0);
// error_reporting(0);
// ====================================================================================

// Important: Ensure there is NO closing ?> tag if this is the only PHP code in the file,
// or if there's any whitespace/newlines after it, as that can cause "headers already sent" errors
// or unwanted output before the JSON.
// If you MUST have a closing tag, ensure there's absolutely no whitespace after it.
// Example: No whitespace here --> ?>