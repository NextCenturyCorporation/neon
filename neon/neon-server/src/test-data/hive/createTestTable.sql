create table integrationTest.records(id INT,
			  donation_date BIGINT,
			  amount DOUBLE,
			  charity_id INT,
			  charity_name STRING,
			  donor_id INT,
			  donor_city STRING,
			  donor_state STRING,
			  donor_firstname STRING,
			  donor_lastname STRING)
row format delimited fields terminated by '44'
