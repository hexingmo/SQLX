
-- Create area table
CREATE TABLE IF NOT EXISTS area (
  id BIGINT PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

-- Insert test data
INSERT INTO area (id, name) VALUES
  (1, 'East'),
  (2, 'South');

-- Create department table
CREATE TABLE IF NOT EXISTS department (
  id BIGINT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  area_id INT NOT NULL
);

-- Insert test data
INSERT INTO department (id, name, area_id) VALUES
  (1, 'Research and Development', 1),
  (2, 'Sales', 2);

-- Create employee table
CREATE TABLE IF NOT EXISTS employee (
  id BIGINT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  department_id INT NOT NULL
);

-- Insert test data
INSERT INTO employee (id, name, department_id) VALUES
  (1, 'John Doe', 1),
  (2, 'Jane Doe', 1),
  (3, 'Bob Smith', 2),
  (4, 'Alice Jones', 2);

-- Create salary table
CREATE TABLE salary (
  employee_id INT PRIMARY KEY,
  amount DECIMAL(10, 2) NOT NULL,
  date DATE NOT NULL
);

-- Insert test data
INSERT INTO salary (employee_id, amount, date) VALUES
  (1, 10000.00, '2023-05-01'),
  (2, 12000.00, '2023-05-01'),
  (3, 8000.00, '2023-05-01'),
  (4, 9000.00, '2023-05-01');

