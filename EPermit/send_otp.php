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

if (empty($email)) {
    echo json_encode(["success" => false, "message" => "Email is required"]);
    exit();
}

// Get OTP from DB
$stmt = $conn->prepare("SELECT otp FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->bind_result($otp);
if ($stmt->fetch()) {
    $stmt->close();

    if (empty($otp)) {
        echo json_encode(["success" => false, "message" => "OTP not found for this email"]);
        $conn->close();
        exit();
    }

    $to = $email;
    $subject = "Your OTP Code";
    $message = "Your One-Time Password (OTP) is: $otp\n\nPlease enter this OTP to verify your email.";
    $headers = "From: no-reply@yourdomain.com"; // Change this to your domain/email

    if (mail($to, $subject, $message, $headers)) {
        echo json_encode(["success" => true, "message" => "OTP sent to email"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to send OTP email"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Email not found"]);
}

$conn->close();
?>
