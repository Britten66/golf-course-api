INSERT INTO members (name, address, email, phone, membership_start_date, membership_duration, membership_type) VALUES
('Alice Morrison', '12 Fairway Lane, Halifax NS', 'alice.morrison@example.com', '902-555-0134', '2026-01-15', 12, 'ANNUAL'),
('Brian Chen', '88 Bunker Road, Dartmouth NS', 'brian.chen@example.com', '902-555-0198', '2026-03-01', 1, 'MONTHLY'),
('Carla Nguyen', '5 Green Street, Truro NS', 'carla.nguyen@example.com', '902-555-0221', '2024-06-10', 240, 'LIFETIME'),
('Daniel Okafor', '301 Putter Crescent, Sydney NS', 'daniel.okafor@example.com', '782-555-0410', '2026-02-20', 12, 'ANNUAL'),
('Erin MacDonald', '47 Caddie Court, Halifax NS', 'erin.macdonald@example.com', '902-555-0777', '2026-05-05', 6, 'MONTHLY'),
('Frank Alice Delgado', '9 Birdie Way, Bedford NS', 'frank.delgado@example.com', '782-555-0333', '2025-11-11', 12, 'ANNUAL');

INSERT INTO tournaments (start_date, end_date, location, entry_fee, cash_prize) VALUES
('2026-08-14', '2026-08-16', 'Glen Arbour Golf Course', 150.00, 5000.00),
('2026-08-14', '2026-08-15', 'Ashburn Golf Club', 100.00, 2500.00),
('2026-09-05', '2026-09-07', 'Fox Harbour Resort', 250.00, 10000.00),
('2026-10-02', '2026-10-03', 'Glen Arbour Golf Course', 75.00, 1200.00);

INSERT INTO member_tournament (member_id, tournament_id) VALUES
(1, 1),
(1, 3),
(2, 1),
(3, 2),
(4, 3),
(5, 4);
