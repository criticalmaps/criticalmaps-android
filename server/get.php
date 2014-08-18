<?php

require("topsykretts.php");

if (mysqli_connect_errno()) {
    die ("Failed to connect to MySQL:");
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

if (isset($_GET["device"]) && array_key_exists($_GET["device"], $locationsArray)) {
    unset($locationsArray[$_GET["device"]]);
}

echo json_encode($locationsArray);

if (isset($_GET["device"]) && isset($_GET["longitude"]) && isset($_GET["latitude"])) {
    $query = "INSERT INTO `db539887603`.`Locations` (`id`, `device`, `timestamp`, `longitude`, `latitude`) VALUES (NULL, '" . $_GET['device'] . "', CURRENT_TIMESTAMP, '" . $_GET["longitude"] . "', '" . $_GET["latitude"] . "' );";
    mysqli_query($con, $query);
}

mysqli_close($con);

?>