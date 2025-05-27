<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "E-permit";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Connection failed: " . $conn->connect_error]);
    exit();
}

$email = $_POST['email'] ?? '';
$otp = $_POST['otp'] ?? '';

if (empty($email) || empty($otp)) {
    echo json_encode(["success" => false, "message" => "Email and OTP are required"]);
    exit();
}

// Check OTP
$stmt = $conn->prepare("SELECT otp, is_verified FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->bind_result($db_otp, $is_verified);

if ($stmt->fetch()) {
    $stmt->close();

    if ($is_verified == 1) {
        echo json_encode(["success" => false, "message" => "Email is already verified."]);
        $conn->close();
        exit();
    }

    if ($otp === $db_otp) {
        // Correct OTP, update is_verified and clear OTP
        $update_stmt = $conn->prepare("UPDATE users SET is_verified = 1, otp = NULL WHERE email = ?");
        $update_stmt->bind_param("s", $email);
        if ($update_stmt->execute()) {
            echo json_encode(["success" => true, "message" => "Email verified successfully!"]);
        } else {
            echo json_encode(["success" => false, "message" => "Failed to update verification status."]);
        }
        $update_stmt->close();
    } else {
        echo json_encode(["success" => false, "message" => "Invalid OTP. Please try again."]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Email not found."]);
}

$conn->close();
?>
