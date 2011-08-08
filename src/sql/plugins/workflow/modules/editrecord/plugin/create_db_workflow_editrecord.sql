--
-- Table structure for table task_edit_record_cf
--
DROP TABLE IF EXISTS task_edit_record_cf;
CREATE TABLE task_edit_record_cf(
  id_task INT(11) DEFAULT 0 NOT NULL,
  id_state_after_edition INT(11) DEFAULT 0 NOT NULL,
  PRIMARY KEY (id_task)
);

--
-- Table structure for table task_edit_record
--
DROP TABLE IF EXISTS task_edit_record;
CREATE TABLE task_edit_record(
  id_edit_record INT(11) DEFAULT 0 NOT NULL,
  id_record INT(11) DEFAULT 0 NOT NULL,
  id_task INT(11) DEFAULT 0 NOT NULL,
  message VARCHAR(255) DEFAULT '' NOT NULL,
  PRIMARY KEY (id_record, id_task)
);

--
-- Table structure for task_edit_record_value
--
DROP TABLE IF EXISTS task_edit_record_value;
CREATE TABLE task_edit_record_value(
  id_edit_record INT(11) DEFAULT 0 NOT NULL,
  id_entry INT(11) DEFAULT 0 NOT NULL,
  PRIMARY KEY (id_edit_record, id_entry)
);
