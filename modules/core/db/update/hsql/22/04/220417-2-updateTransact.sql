alter table FETCHASSESS_TRANSACT add column SPENT boolean ^
update FETCHASSESS_TRANSACT set SPENT = false where SPENT is null ;
alter table FETCHASSESS_TRANSACT alter column SPENT set not null ;
