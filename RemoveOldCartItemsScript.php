<?php
try {
    $db_name     = 'DBNAME';
    $db_user     = 'USER';
    $db_password = 'PSW';
    $db_host     = 'IP';

    $pdo = new PDO('mysql:host=' . $db_host . '; dbname=' . $db_name, $db_user, $db_password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);

    $sql  = "delete from Carrello where dataOra <= (NOW() - INTERVAL 30 MINUTE) AND selled = 0;";


    $stmt = $pdo->prepare($sql);
    $stmt->execute($data);
    $row = $stmt->rowCount();
    if($row>0){
    echo ('Deleted rows: ' .$row.' ');
    echo "\nCurrent datetime is " . date("d/m/Y H:i:s");
    echo (date_default_timezone_get());
}
} catch (PDOException $ex) {
    echo $ex->getMessage();
}
