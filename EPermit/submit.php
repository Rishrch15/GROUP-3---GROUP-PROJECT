<?php
include 'db_connect.php';

header('Content-Type: application/json');

$response = ["success" => false, "message" => ""];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $input = json_decode(file_get_contents('php://input'), true);

    $project_name = $input['project_name'] ?? '';
    $borrower_name = $input['borrower_name'] ?? '';
    $user_id = $input['user_id'] ?? NULL;
    $department = $input['department'] ?? '';
    $date_of_project = $input['date_of_project'] ?? '';
    $time_of_project = $input['time_of_project'] ?? '';
    $venue = $input['venue'] ?? '';
    $items_data = $input['items'] ?? [];

    $status = "Pending";
    $date_submitted = date('Y-m-d H:i:s');

    if (empty($project_name) || empty($borrower_name) || empty($department) || empty($date_of_project) || empty($time_of_project) || empty($venue)) {
        $response["message"] = "All core request details (project name, borrower name, department, date/time of project, venue) are required.";
        echo json_encode($response);
        exit;
    }

    $stmt = $conn->prepare("INSERT INTO borrow_requests (project_name, borrower_name, status, date_submitted, user_id, department, date_of_project, time_of_project, venue) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("ssssissss", $project_name, $borrower_name, $status, $date_submitted, $user_id, $department, $date_of_project, $time_of_project, $venue);

    if ($stmt->execute()) {
        $request_id = $conn->insert_id;

        if (!empty($items_data) && is_array($items_data)) {
            $items_inserted = 0;
            $items_failed = 0;
            foreach ($items_data as $item) {
                $qty = $item['qty'] ?? 0;
                $description = $item['description'] ?? '';
                $date_of_transfer = $item['date_of_transfer'] ?? NULL;
                $location_from = $item['location_from'] ?? '';
                $location_to = $item['location_to'] ?? '';

                $item_stmt = $conn->prepare("INSERT INTO request_items (request_id, qty, description, date_of_transfer, location_from, location_to) VALUES (?, ?, ?, ?, ?, ?)");
                $item_stmt->bind_param("iissss", $request_id, $qty, $description, $date_of_transfer, $location_from, $location_to);
                if ($item_stmt->execute()) {
                    $items_inserted++;
                } else {
                    $items_failed++;
                    error_log("Failed to insert item for request $request_id: " . $item_stmt->error);
                }
                $item_stmt->close();
            }
            $response["message"] = "Borrow request submitted successfully. Inserted $items_inserted items.";
            if ($items_failed > 0) {
                $response["message"] .= " ($items_failed items failed to insert.)";
            }
        } else {
            $response["message"] = "Borrow request submitted successfully (no items provided).";
        }

        $response["success"] = true;
        $response["request_id"] = $request_id;
    } else {
        $response["message"] = "Failed to submit request: " . $stmt->error;
    }

    $stmt->close();
} else {
    $response["message"] = "Invalid request method.";
}

$conn->close();
echo json_encode($response);
?>