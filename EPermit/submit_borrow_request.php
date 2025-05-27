<?php

require_once 'db_connect.php';
header('Content-Type: application/json'); 
$response = array();
$json_data = file_get_contents('php://input');
$data = json_decode($json_data, true); 

if (isset($data['dateSubmitted']) && isset($data['department']) && isset($data['borrowerName']) &&
    isset($data['gender']) && isset($data['projectName']) && isset($data['dateOfProject']) &&
    isset($data['timeOfProject']) && isset($data['venue']) && isset($data['borrowerUniqueId'])) {

    $dateSubmitted = $conn->real_escape_string($data['dateSubmitted']);
    $department = $conn->real_escape_string($data['department']);
    $borrowerName = $conn->real_escape_string($data['borrowerName']);
    $gender = $conn->real_escape_string($data['gender']);
    $projectName = $conn->real_escape_string($data['projectName']);
    $dateOfProject = $conn->real_escape_string($data['dateOfProject']);
    $timeOfProject = $conn->real_escape_string($data['timeOfProject']);
    $venue = $conn->real_escape_string($data['venue']);
    $borrowerUniqueId = $conn->real_escape_string($data['borrowerUniqueId']); 

    $status = 'Pending';
    $approvedBy = null; 

    $conn->begin_transaction();

    try {
    
        $sql_insert_request = "INSERT INTO borrow_requests (date_submitted, department, borrower_name, gender,
                                project_name, date_of_project, time_of_project, venue, status, approved_by, borrower_unique_id)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if ($stmt_request = $conn->prepare($sql_insert_request)) {
            $stmt_request->bind_param("sssssssssss",
                $dateSubmitted, $department, $borrowerName, $gender, $projectName,
                $dateOfProject, $timeOfProject, $venue, $status, $approvedBy, $borrowerUniqueId); 

            if ($stmt_request->execute()) {
                $new_request_id = $conn->insert_id;

                if (isset($data['items']) && is_array($data['items']) && !empty($data['items'])) {
                    $sql_insert_item = "INSERT INTO borrow_request_items (request_id, qty, description,
                                            date_of_transfer, location_from, location_to)
                                        VALUES (?, ?, ?, ?, ?, ?)";
                    if ($stmt_item = $conn->prepare($sql_insert_item)) {
                        foreach ($data['items'] as $item) {
                            $qty = $conn->real_escape_string($item['qty']);
                            $description = $conn->real_escape_string($item['description']);
                            $dateOfTransfer = $conn->real_escape_string($item['dateOfTransfer']);
                            $locationFrom = $conn->real_escape_string($item['locationFrom']);
                            $locationTo = $conn->real_escape_string($item['locationTo']);
                            $stmt_item->bind_param("iissss", $new_request_id, $qty, $description,
                                $dateOfTransfer, $locationFrom, $locationTo);
                            $stmt_item->execute();

                            if ($stmt_item->errno) {
                                throw new Exception("Error inserting item for request_id " . $new_request_id . ": " . $stmt_item->error);
                            }
                        }
                        $stmt_item->close();
                    } else {
                        throw new Exception("Failed to prepare item insertion statement: " . $conn->error);
                    }
                }

                $conn->commit();
                $response["success"] = true;
                $response["message"] = "Borrow request submitted successfully!";
                $response["request_id"] = $new_request_id; 
            } else {
                throw new Exception("Failed to insert main borrow request: " . $stmt_request->error);
            }
            $stmt_request->close();
        } else {
            throw new Exception("Failed to prepare main request insertion statement: " . $conn->error);
        }
    } catch (Exception $e) {
        $conn->rollback(); 
        $response["success"] = false;
        $response["message"] = "Error submitting request: " . $e->getMessage();
        error_log("Borrow Request Submission Fatal Error: " . $e->getMessage()); 
    }
} else {
    $response["success"] = false;
    $missing_params = [];
    if (!isset($data['dateSubmitted'])) $missing_params[] = 'dateSubmitted';
    if (!isset($data['department'])) $missing_params[] = 'department';
    if (!isset($data['borrowerName'])) $missing_params[] = 'borrowerName';
    if (!isset($data['gender'])) $missing_params[] = 'gender';
    if (!isset($data['projectName'])) $missing_params[] = 'projectName';
    if (!isset($data['dateOfProject'])) $missing_params[] = 'dateOfProject';
    if (!isset($data['timeOfProject'])) $missing_params[] = 'timeOfProject';
    if (!isset($data['venue'])) $missing_params[] = 'venue';
    if (!isset($data['borrowerUniqueId'])) $missing_params[] = 'borrowerUniqueId';

    $response["message"] = "Missing one or more required main request parameters: " . implode(", ", $missing_params);
}

echo json_encode($response);

$conn->close();
?>