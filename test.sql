/*
SQLyog Ultimate v11.24 (32 bit)
MySQL - 5.6.21-log : Database - spider
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`spider` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `spider`;

/*Table structure for table `thread` */

DROP TABLE IF EXISTS `thread`;

CREATE TABLE `thread` (
  `_id` int(11) NOT NULL AUTO_INCREMENT,
  `path` char(255) NOT NULL,
  `threadtype` int(1) NOT NULL,
  `hashcode` int(20) NOT NULL,
  `contenttype` int(1) DEFAULT NULL,
  PRIMARY KEY (`_id`),
  UNIQUE KEY `hashcode` (`hashcode`)
) ENGINE=InnoDB AUTO_INCREMENT=36426 DEFAULT CHARSET=utf8;
