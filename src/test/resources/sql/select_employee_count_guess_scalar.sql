  select count(*)
    from users
   where employee_id > #{java.lang.Integer}
order by employee_id


