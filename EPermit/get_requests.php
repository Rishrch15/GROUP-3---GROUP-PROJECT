<?php
header('Content-Type: application/json');

// Database configuration
$servername = "localhost";
$username = "root";
$password = " ";
$dbname = "E-permit";

try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }
    
    // Get parameters
    $status = $_GET['status'] ?? '';
    $borrower_id = $_GET['borrower_id'] ?? '';
    
    // Validate
    if (empty($status) || empty($borrower_id)) {
        throw new Exception("Missing required parameters");
    }
    
    // Prepare query (with optional approved_by column)
    $query = "SELECT 
                request_id, 
                date_submitted, 
                department, 
                borrower_name, 
                gender, 
                borrower_id, 
                project_name, 
                date_of_project, 
                time_of_project, 
                venue, 
                status";
                
    // Only include approved_by if column exists
    $checkColumn = $conn->query("SHOW COLUMNS FROM requests LIKE 'approved_by'");
    if ($checkColumn->num_rows > 0) {
        $query .= ", approved_by";
    }
    
    $query .= " FROM requests WHERE status = ? AND borrower_id = ?";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ss", $status, $borrower_id);
    $stmt->execute();
    
    $result = $stmt->get_result();
    $requests = $result->fetch_all(MYSQLI_ASSOC);
    
    // Get items for each request
    foreach ($requests as &$request) {
        $stmt = $conn->prepare("SELECT * FROM items WHERE request_id = ?");
        $stmt->bind_param("i", $request['request_id']);
        $stmt->execute();
        $itemsResult = $stmt->get_result();
        $request['items'] = $itemsResult->fetch_all(MYSQLI_ASSOC);
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'Requests retrieved successfully',
        'requests' => $requests
    ]);
    
} catch(Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
} finally {
    if (isset($conn)) {
        $conn->close();
    }
}
?>