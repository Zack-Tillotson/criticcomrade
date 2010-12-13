#!/bin/bash

start=`date +%s`

echo "===================================="
echo "===   starting comradery regen   ==="
echo "=== `date` ==="
echo "===================================="

dbname="critic_review_new"
dbuser="critic_review"
dbpass="critic_review_pwd"

# Regen the critic - user comradery
mysql -u $dbuser --password=$dbpass $dbname -e  \
   "delete from user_critic_comradery"
mysql -u $dbuser --password=$dbpass $dbname -e  \
   "insert into user_critic_comradery select    ur.user_id, c.api_id critic_id    ,pow(case when I > 10.9 then pow(S/I, 2.5)*20.2+6.1 else 0.01 end, 9.8)/10000000 comeradery ,I intersect_c, S matching_c     from  critics c    ,(select critic_id, count(*) N from c_reviews cr group by critic_id) cr    ,(select critic_id, iur.user_id, count(*) I, sum(case when case when icr.score >= (select cutoff from review_pos_cutoffs where source = icr.source) then 1 else 0 end = case when iur.score >= (select cutoff from review_pos_cutoffs where source = 'user') then 1 else 0 end then 1 else 0 end) S from c_reviews icr, u_reviews iur where icr.movie_id = iur.movie_id group by critic_id, user_id) ur    ,(select user_id, count(*) R from u_reviews group by user_id) urc where        c.critic_id = cr.critic_id    and c.critic_id = ur.critic_id    and cr.N > 15 group by    ur.user_id, c.api_id"

# Regen the critic - user - movie cccp scores
mysql -u $dbuser --password=$dbpass $dbname -e  \
   "delete from user_critic_movie_comrade_score"
mysql -u $dbuser --password=$dbpass $dbname -e  \
   "insert into user_critic_movie_comrade_score select cl.user_id, cr.movie_id, format(sum(cr.score * cl.comradery) / sum(cl.comradery), 4) comrade_score, count(cr.score) comrade_c from (select   m.movie_id, c.api_id cid,    case when r.score >= (select cutoff from review_pos_cutoffs where source = r.source) then 1 else 0 end score from    movies m,   c_reviews r,    critics c where    m.movie_id = r.movie_id and   c.critic_id = r.critic_id) cr, user_critic_comradery cl   where    cr.cid = cl.cid group by   cl.user_id, cr.movie_id"


end=`date +%s`

minutestaken=`expr \( $end - $start \) / 60`

echo "===================================="
echo "==== total time : `printf "%3d" $minutestaken` minutes ======"
echo "===================================="
echo ""
echo ""
