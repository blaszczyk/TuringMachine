
INSERT INTO `program` (`program_id`, `name`, `start_id`, `timestamp_update`) VALUES
(1, 'PingPong', 1, '2017-07-09 19:31:21');


INSERT INTO `state` (`state_id`, `name`, `timestamp_update`) VALUES
(1, 'go right', '2017-07-09 19:30:48'),
(2, 'go left', '2017-07-09 19:31:43');


INSERT INTO `status` (`status_id`, `running`, `currentCell_id`, `currentState_id`, `timestamp_update`) VALUES
(1, 1, 8, 1, '2017-07-09 19:28:29');


INSERT INTO `step` (`step_id`, `readValue`, `writeValue`, `direction`, `stateFrom_id`, `stateTo_id`, `timestamp_update`) VALUES
(1, 0, 0, 1, 1, 1, '2017-07-09 19:33:02'),
(2, 1, 1, 0, 1, 2, '2017-07-09 19:33:35'),
(3, 0, 0, 0, 2, 2, '2017-07-09 19:34:00'),
(4, 1, 1, 1, 2, 1, '2017-07-09 19:34:19');


INSERT INTO `tapecell` (`tapeCell_id`, `value`, `next_id`, `timestamp_update`) VALUES
(1, 1, 2, '2017-07-09 19:28:43'),
(2, 0, 3, '2017-07-09 19:29:04'),
(3, 0, 4, '2017-07-09 19:29:22'),
(4, 0, 5, '2017-07-09 19:29:34'),
(5, 0, 6, '2017-07-09 19:29:48'),
(6, 0, 7, '2017-07-09 19:30:01'),
(7, 0, 8, '2017-07-09 19:30:12'),
(8, 1, NULL, '2017-07-09 19:30:26');


INSERT INTO `turingmachine` (`turingMachine_id`, `name`, `status_id`, `program_id`, `timestamp_update`) VALUES
(1, 'TM1', 1, 1, '2017-07-09 19:28:13');
