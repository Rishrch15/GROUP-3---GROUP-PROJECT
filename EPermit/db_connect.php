<?php
$servername = "localhost";
$username = "root"; 
$password = "";  
$dbname = "e-permit"; 

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode([
        "success" => false,
        "message" => "Connection failed: " . $conn->connect_error
    ]));
}

$conn->set_charset("utf8");
?>
