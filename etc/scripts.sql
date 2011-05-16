drop sequence CUSTOMERS_SEQ;
drop table CUSTOMERS;

create sequence CUSTOMERS_SEQ start with 0 increment by 1;
create table CUSTOMERS (
  customerId bigint not null primary key,
  firstName varchar(256),
  lastName varchar(256),
  address varchar(256),
  password varchar(256),
  gender varchar(256),
  nationality varchar(256)
);

drop sequence ACCOUNTS_SEQ;
drop table ACCOUNTS;

create sequence ACCOUNTS_SEQ start with 0 increment by 1;
create table ACCOUNTS (
  accountId bigint not null primary key,
  balance DOUBLE,
  accountType varchar(256),
  interest FLOAT,
  creditLimit DOUBLE,
  accountStatus integer,
  customerId bigint
);

insert into customers values (1, null, null, null, null, null, 'tZxnvxlqR1gZHkL3ZnDOug==', 'admin');
insert into user_roles values (1, 'admin', 'administrator');
