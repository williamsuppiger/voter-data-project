-- MySQL dump 10.13  Distrib 8.0.22, for macos10.15 (x86_64)
--
-- Host: localhost    Database: voter_data
-- ------------------------------------------------------
-- Server version	8.0.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ct_clean`
--

DROP TABLE IF EXISTS `ct_clean`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_clean` (
  `town_id` varchar(3) DEFAULT NULL,
  `voter_id` varchar(9) NOT NULL,
  `last_name` varchar(35) DEFAULT NULL,
  `first_name` varchar(20) DEFAULT NULL,
  `middle_name` varchar(15) DEFAULT NULL,
  `name_prefix` varchar(5) DEFAULT NULL,
  `name_suffix` varchar(5) DEFAULT NULL,
  `cd_status_code` varchar(1) DEFAULT NULL,
  `cd_off_reason` varchar(1) DEFAULT NULL,
  `voting_district` varchar(3) DEFAULT NULL,
  `voting_precinct` varchar(3) DEFAULT NULL,
  `state_congress_code` varchar(3) DEFAULT NULL,
  `state_senate_code` varchar(3) DEFAULT NULL,
  `state_assembly_code` varchar(3) DEFAULT NULL,
  `address_number` varchar(6) DEFAULT NULL,
  `address_unit` varchar(8) DEFAULT NULL,
  `street_name` varchar(40) DEFAULT NULL,
  `town_name` varchar(18) DEFAULT NULL,
  `state` varchar(2) DEFAULT NULL,
  `zip5` varchar(5) DEFAULT NULL,
  `zip4` varchar(4) DEFAULT NULL,
  `dob` varchar(10) DEFAULT NULL,
  `phone_number` varchar(10) DEFAULT NULL,
  `party_code` varchar(5) DEFAULT NULL,
  `unqualified_party_code` varchar(5) DEFAULT NULL,
  `gender` varchar(1) DEFAULT NULL,
  `registration_date` varchar(10) DEFAULT NULL,
  `election_history` text,
  PRIMARY KEY (`voter_id`),
  UNIQUE KEY `voter_id_UNIQUE` (`voter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_geo_census`
--

DROP TABLE IF EXISTS `ct_geo_census`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_geo_census` (
  `voter_id` varchar(9) NOT NULL,
  `match_indicator` varchar(8) DEFAULT NULL,
  `match_type` varchar(9) DEFAULT NULL,
  `pt` point DEFAULT NULL,
  `tigerline_id` varchar(15) DEFAULT NULL,
  `tigerline_id_side` varchar(1) DEFAULT NULL,
  `geo_source_info` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`voter_id`),
  UNIQUE KEY `voter_id_UNIQUE` (`voter_id`),
  CONSTRAINT `fk_ctGeoCensus_ctClean` FOREIGN KEY (`voter_id`) REFERENCES `ct_clean` (`voter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_geo_geocodio`
--

DROP TABLE IF EXISTS `ct_geo_geocodio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_geo_geocodio` (
  `voter_id` varchar(9) NOT NULL,
  `pt` point DEFAULT NULL,
  `accuracy` float DEFAULT NULL,
  `accuracy_type` varchar(30) DEFAULT NULL,
  `source` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`voter_id`),
  UNIQUE KEY `voter_id_UNIQUE` (`voter_id`),
  CONSTRAINT `fk_ctGeoGeocodio_ctClean` FOREIGN KEY (`voter_id`) REFERENCES `ct_clean` (`voter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_geo_main`
--

DROP TABLE IF EXISTS `ct_geo_main`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_geo_main` (
  `voter_id` varchar(9) NOT NULL,
  `pt` point NOT NULL,
  `geo_source` int DEFAULT NULL COMMENT '1: census data',
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `geo_pt` point /*!80003 SRID 4326 */ DEFAULT NULL,
  PRIMARY KEY (`voter_id`),
  UNIQUE KEY `voter_id_UNIQUE` (`voter_id`),
  KEY `lat_lng` (`latitude`,`longitude`),
  KEY `lng_lat` (`longitude`,`latitude`),
  CONSTRAINT `fk_ctGeoMain_ctClean` FOREIGN KEY (`voter_id`) REFERENCES `ct_clean` (`voter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'voter_data'
--

--
-- Dumping routines for database 'voter_data'
--
/*!50003 DROP FUNCTION IF EXISTS `GCDistDeg` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE  FUNCTION `GCDistDeg`(
        _lat1 DOUBLE,
        _lon1 DOUBLE,
        _lat2 DOUBLE,
        _lon2 DOUBLE
    ) RETURNS double
    DETERMINISTIC
    SQL SECURITY INVOKER
    COMMENT 'Degrees in, Degrees out.  For conversion: 69.172 mi/deg or 111.325 km/deg'
BEGIN

    DECLARE _deg2rad DOUBLE DEFAULT PI()/180;
    DECLARE _rlat1 DOUBLE DEFAULT _deg2rad * _lat1;
    DECLARE _rlat2 DOUBLE DEFAULT _deg2rad * _lat2;

    DECLARE _rlond DOUBLE DEFAULT _deg2rad * (_lon1 - _lon2);
    DECLARE _m     DOUBLE DEFAULT COS(_rlat2);
    DECLARE _x     DOUBLE DEFAULT COS(_rlat1) - _m * COS(_rlond);
    DECLARE _y     DOUBLE DEFAULT               _m * SIN(_rlond);
    DECLARE _z     DOUBLE DEFAULT SIN(_rlat1) - SIN(_rlat2);
    DECLARE _n     DOUBLE DEFAULT SQRT(
                        _x * _x +
                        _y * _y +
                        _z * _z    );
    RETURN  2 * ASIN(_n / 2) / _deg2rad;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ct_ClostestRecords` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE  PROCEDURE `sp_ct_ClostestRecords`(  
	IN my_lat DOUBLE,     
	IN my_lng DOUBLE,     
	IN radius_miles INT,     
    IN query_limit INT,
    IN twoParty BOOL
)
BEGIN 
SELECT ct_geo_main.voter_id,      
	GCDistDeg(my_lat, my_lng, 
	ct_geo_main.latitude, 
	ct_geo_main.longitude) * 69.172 AS miles,        
	ct_clean.party_code,        
	ct_clean.first_name,
	ct_clean.last_name,
    ct_clean.dob
	FROM ct_geo_main  
INNER JOIN ct_clean  
	ON ct_geo_main.voter_id = ct_clean.voter_id     
WHERE 
	ct_geo_main.latitude BETWEEN my_lat - radius_miles/69.172
    AND my_lat + radius_miles/69.172
    AND ct_geo_main.longitude BETWEEN my_lng - radius_miles/69.172 / COS(RADIANS(my_lat))
    AND my_lng + radius_miles/69.172 / COS(RADIANS(my_lat))
	AND (IF(twoParty, ct_clean.party_code, 'R') = 'R' OR IF(twoParty, ct_clean.party_code, 'D') = 'D')
HAVING miles <= radius_miles     
ORDER BY miles     
LIMIT query_limit;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ct_VoterInfo` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE  PROCEDURE `sp_ct_VoterInfo`(
	IN voter_id VARCHAR(10)
)
BEGIN
	SELECT * FROM ct_clean WHERE ct_clean.voter_id = voter_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-01-22 12:29:34
