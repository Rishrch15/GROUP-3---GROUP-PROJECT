<?php
include 'db_connect.php';

header('Content-Type: application/json');

$response = ["success" => false, "message" => ""];

if ($_SERVER["REQUEST_METHOD"] == "GET") {
    $request_id = $_GET['request_id'] ?? '';

    if (empty($request_id)) {
        $response["message"] = "Request ID is required.";
        echo json_encode($response);
        exit;
    }

    $sql = "SELECT br.request_id, br.project_name, br.borrower_name, br.status, br.date_submitted,
                   br.approved_by, br.date_of_project, br.time_of_project, br.venue, br.department,
                   u.gender, br.user_id
            FROM borrow_requests br
            LEFT JOIN users u ON br.user_id = u.id
            WHERE br.request_id = ?";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $request_id);

    if ($stmt->execute()) {
        $result = $stmt->get_result();
        if ($result->num_rows == 1) {
            $request = $result->fetch_assoc();
            $items = [];
            $items_sql = "SELECT item_id, qty, description, date_of_transfer, location_from, location_to
                          FROM request_items WHERE request_id = ?";
            $items_stmt = $conn->prepare($items_sql);
            $items_stmt->bind_param("i", $request_id);
            if ($items_stmt->execute()) {
                $items_result = $items_stmt->get_result();
                while ($item_row = $items_result->fetch_assoc()) {
                    $items[] = $item_row;
                }
            } else {
                error_log("Failed to fetch items for request_id $request_id: " . $items_stmt->error);
            }
            $items_stmt->close();
            $request['items'] = $items;

            $response["success"] = true;
            $response["message"] = "Request details fetched successfully.";
            $response["request"] = $request; 
        } else {
            $response["message"] = "Request not found.";
        }
    } else {
        $response["message"] = "Failed to fetch request details: " . $stmt->error;
    }

    $stmt->close();
} else {
    $response["message"] = "Invalid request method.";
}

$conn->close();
echo json_encode($response);
?>