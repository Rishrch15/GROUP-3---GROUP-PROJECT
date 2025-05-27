<?php
include 'db_connect.php';

header('Content-Type: application/json');
$response = ["success" => false, "message" => ""];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $input = json_decode(file_get_contents('php://input'), true);
    $request_id = $input['request_id'] ?? '';
    $new_status = $input['status'] ?? '';
    $approved_by = $input['approved_by'] ?? NULL;

    if (empty($request_id) || empty($new_status)) {
        $response["message"] = "Request ID and new status are required.";
        echo json_encode($response);
        exit;
    }

    if (!in_array($new_status, ['Approved', 'Rejected', 'Pending'])) {
        $response["message"] = "Invalid status provided. Must be 'Approved', 'Rejected', or 'Pending'.";
        echo json_encode($response);
        exit;
    }

    $stmt = $conn->prepare("UPDATE borrow_requests SET status = ?, approved_by = ? WHERE request_id = ?");
    $stmt->bind_param("ssi", $new_status, $approved_by, $request_id);

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            $response["success"] = true;
            $response["message"] = "Request status updated successfully to " . $new_status . ".";
        } else {
            $response["message"] = "No request found with the given ID, or status was already " . $new_status . ".";
        }
    } else {
        $response["message"] = "Failed to update request status: " . $stmt->error;
    }

    $stmt->close();
} else {
    $response["message"] = "Invalid request method.";
}

$conn->close();
echo json_encode($response);
?>