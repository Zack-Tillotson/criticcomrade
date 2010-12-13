create table ai_log
(
     run_id int primary key auto_increment
    ,search_technique text
    ,ai_version_id int not null
    ,random_seed bigint
    ,start_date datetime not null
    ,end_date datetime not null
    ,fitness_calculation_count int
    ,fitness double not null
);

create table ai_versions
(
    ai_version_id int primary key auto_increment
    ,name text not null
);

create table ai_vars
(
    ai_var_id int primary key auto_increment
    ,ai_version_id int not null
    ,var_name text not null
    ,max_value double
    ,min_value double
    ,initial_max_value double
    ,initial_min_value double
    ,granularity double
    ,active_flg bool not null default 0
);

create table ai_var_instances
(
    ai_var_instance_id int primary key auto_increment
    ,ai_var_id int not null
    ,run_id int not null
    ,value double not null
);