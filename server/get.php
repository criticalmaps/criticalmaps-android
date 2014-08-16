<?php

require("topsykretts.php");

if (mysqli_connect_errno()) {
    echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

$result = mysqli_query($con, "SELECT * FROM Locations WHERE timestamp > (NOW() - INTERVAL 5 MINUTE)");

$locationsArray = [];

while ($row = mysqli_fetch_array($result)) {
    if (array_key_exists($row['device'], $locationsArray)) {
        if (strtotime($locationsArray[$row['device']]['timestamp']) < strtotime($row['timestamp'])) {
            $locationsArray[$row['device']] = array(
                'longitude' => $row['longitude'],
                'latitude' => $row['latitude'],
                'timestamp' => strtotime($row['timestamp']));
        }
    } else {
        $locationsArray[$row['device']] = array(
            'longitude' => $row['longitude'],
            'latitude' => $row['latitude'],
            'timestamp' => strtotime($row['timestamp']));
    }
}

echo json_encode($locationsArray);

mysqli_query($con, "INSERT INTO `db539887603`.`Locations` (`id`, `device`, `timestamp`, `longitude`, `latitude`)
VALUES (NULL, '123123123', CURRENT_TIMESTAMP, '123213', '123213');");

mysqli_close($con);

?>