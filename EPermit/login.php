<?php
ini_set('display_errors', 0); 
ini_set('log_errors', 1);   
error_reporting(E_ALL);     

header('Content-Type: application/json; charset=utf-8');

$response = ['success' => false, 'message' => ''];

// Database configuration
$servername = "localhost"; 
$username = "root";        
$password = "";            
$dbname = "e-permit";       

try {
    // Create database connection
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    // Check connection
    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }

    // Check if POST request
    if ($_SERVER['REQUEST_METHOD'] != 'POST') {
        throw new Exception("Invalid request method. Only POST is allowed.");
    }

    // Get input data
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';
    
    // Validate inputs
    if (empty($email) || empty($password)) {
        throw new Exception("Email and password are required.");
    }

    // Prepare SQL to prevent SQL injection
    $stmt = $conn->prepare("SELECT id, password, role FROM users WHERE email = ?");
    if (!$stmt) {
        throw new Exception("Database error: " . $conn->error);
    }
    
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("Invalid email or password.");
    }

    $user = $result->fetch_assoc();
    
    // Verify password
    if (!password_verify($password, $user['password'])) {
        throw new Exception("Invalid email or password.");
    }

    // Login successful
    $response = [
        'success' => true,
        'message' => 'Login successful!',
        'user' => [
            'id' => $user['id'],
            'role' => $user['role']
        ]
    ];

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Error: " . $e->getMessage());
    $response['message'] = $e->getMessage();
}

echo json_encode($response);
?>