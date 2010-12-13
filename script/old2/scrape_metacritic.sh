#!/bin/bash

start=`date +%s`

echo "===================================="
echo "=== starting metacritic scratch  ==="
echo "=== `date` ==="
echo "===================================="

site="http://www.metacritic.com/film/weekendboxoffice.shtml"
dbname="critic_review"
dbuser="critic_review"
dbpass="critic_review_pwd"

if [ "x$@" != "x" ]; then site="$@"; fi

htmlfile="`mktemp`"
curl "$site" > $htmlfile 2>/dev/null

linkfile="`mktemp`"
perl ~/Documents/scripts/rottenmeta/parse_metacritic.pl $htmlfile > $linkfile

rm $htmlfile

infofile="`mktemp`"
for ((linec=1; linec<=`wc -l "$linkfile" | awk '{print $1}'`; linec++)); do 

	line="`head -$linec $linkfile | tail -1`"
	curl "$line" > $htmlfile 2>/dev/null
	perl ~/Documents/scripts/rottenmeta/parse_metacritic_film.pl $htmlfile > $infofile

	FILM_TITLE="`head -1 $infofile`"
	FILM_SUMMARY="`head -2 $infofile | tail -1`"
	FILM_RELEASE_DATE="`head -3 $infofile | tail -1`"

	MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
		"select movie_id from movies where title like '$FILM_TITLE'\G" |\
		grep movie_id | awk '{print $2}'`

	if [ "x$MOVIEID" = "x" ]; then

		echo "- $FILM_TITLE"
		echo "    TITLE=$FILM_TITLE"
		echo "    SITE=$line"
		echo "    SUMMARY=`echo $FILM_SUMMARY | cut -c 1-50`..."
		echo "    RELEASEDATE=$FILM_RELEASE_DATE"

		mysql -u $dbuser --password=$dbpass $dbname -e  \
			"insert into movies (title, summary, link, release_date) values \
			('$FILM_TITLE', '$FILM_SUMMARY', '$line', str_to_date('$FILM_RELEASE_DATE', '%M %e, %Y'))"
		MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select movie_id from movies where title like '$FILM_TITLE'\G" |\
			grep movie_id | awk '{print $2}'`

	else
		echo "  $FILM_TITLE"
	fi

	for((i=4; i<=`wc -l $infofile | awk '{print $1'}`; i+=5)); do

		a=$i;
		b=`expr $a + 1`
		c=`expr $b + 1`
		d=`expr $c + 1`
		e=`expr $d + 1`

		REVIEW_NAME="`head -$a $infofile | tail -1`"
		REVIEW_PUBLISHER="`head -$b $infofile | tail -1`"
		REVIEW_SUMMARY="`head -$c $infofile | tail -1`"
		REVIEW_LINK="`head -$d $infofile | tail -1`"
		REVIEW_SCORE="`head -$e $infofile | tail -1`"

		CRITICID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select critic_id from critics where name like '$REVIEW_NAME' and publisher like '$REVIEW_PUBLISHER'\G" |\
			grep critic_id | awk '{print $2}'`
		newcritic=" "

		if [ "x$CRITICID" = "x" ]; then 

			mysql -u $dbuser --password=$dbpass $dbname -e \
				"insert into critics (name, publisher) values ('$REVIEW_NAME', '$REVIEW_PUBLISHER')"
			CRITICID=`mysql -u $dbuser --password=$dbpass $dbname -e \
				"select critic_id from critics where name like '$REVIEW_NAME' and publisher like '$REVIEW_PUBLISHER'\G" |\
				grep critic_id | awk '{print $2}'`
			newcritic="-"

		fi

		reviewindb=`mysql -u $dbuser --password=$dbpass $dbname -e \
			   "select review_id from c_reviews where critic_id = $CRITICID and movie_id = $MOVIEID \G" |\
			   grep review_id | wc -l | awk '{print $1}'`

		if [ $reviewindb -eq 0 ]; then
		
			echo "   $newcritic $REVIEW_SCORE $REVIEW_NAME ($CRITICID)" 

			mysql -u critic_review --password="critic_review_pwd" critic_review -e \
				"insert into c_reviews (movie_id, critic_id, score, summary, link, source) \
				values ($MOVIEID, $CRITICID, $REVIEW_SCORE, '$REVIEW_SUMMARY', '$REVIEW_LINK', 'metacritic')"

		fi

		

	done
done

rm $linkfile
rm $infofile

end=`date +%s`

minutestaken=`expr \( $end - $start \) / 60`

echo "===================================="
echo "==== total time : `printf "%3d" $minutestaken` minutes ======"
echo "===================================="
echo ""
echo ""
