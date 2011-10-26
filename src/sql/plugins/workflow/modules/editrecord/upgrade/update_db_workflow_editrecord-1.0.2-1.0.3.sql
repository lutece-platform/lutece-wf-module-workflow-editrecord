--
-- Change size of task_edit_record.message
--
ALTER TABLE task_edit_record CHANGE message message LONG VARCHAR;

--
-- Add column to task_edit_record_cf
--
ALTER TABLE task_edit_record_cf ADD COLUMN default_message LONG VARCHAR;
