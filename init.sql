DROP TABLE IF EXISTS `t_test`;
CREATE TABLE `t_test` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `create_man` varchar(35) DEFAULT NULL,
  `state` int(2) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `last_update_man` varchar(35) DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;