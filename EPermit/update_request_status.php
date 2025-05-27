<?php
require_once 'db_connect.php'; 
header('Content-Type: application/json'); 
$response = array();

$json_data = file_get_contents('php://input');
$data = json_decode($json_data, true); 

if (isset($data['request_id']) && isset($data['status']) && isset($data['approved_by'])) {
    $request_id = $conn->real_escape_string($data['request_id']);
    $status = $conn->real_escape_string($data['status']);
    $approved_by = $conn->real_escape_string($data['approved_by']);

    if (!in_array($status, ['Approved', 'Rejected'])) {
        $response["success"] = false;
        $response["message"] = "Invalid status provided. Status must be 'Approved' or 'Rejected'.";
        echo json_encode($response);
        $conn->close();
        exit();
    }

    $sql = "UPDATE borrow_requests SET status = ?, approved_by = ? WHERE request_id = ?";

    if ($stmt = $conn->prepare($sql)) {
        $stmt->bind_param("ssi", $status, $approved_by, $request_id); 

        if ($stmt->execute()) {
            if ($stmt->affected_rows > 0) {
                $response["success"] = true;
                $response["message"] = "Request status updated successfully to '" . $status . "'.";
            } else {
                $response["success"] = false;
                $response["message"] = "Request ID " . $request_id . " not found or no change in status was needed.";
            }
        } else {
            $response["success"] = false;
            $response["message"] = "Failed to execute update statement: " . $stmt->error;
        }
        $stmt->close();
    } else {
        $response["success"] = false;
        $response["message"] = "SQL prepare error for update statement: " . $conn->error;
    }
} else {

    $response["success"] = false;
    $response["message"] = "Required parameters (request_id, status, approved_by) are missing in the POST data.";
}

echo json_encode($response);

$conn->close();
?>