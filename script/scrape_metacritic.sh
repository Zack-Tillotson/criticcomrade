#!/bin/bash

start=`date +%s`

echo "===================================="
echo "=== starting metacritic scratch  ==="
echo "=== `date` ==="
echo "===================================="

site="http://www.metacritic.com/film/weekendboxoffice.shtml"
dbname="critic_review_new"
dbuser="critic_review"
dbpass="critic_review_pwd"

if [ "x$@" != "x" ]; then site="$@"; fi

htmlfile="`mktemp`"
tmpfile="`mktemp`"

curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$site" > $htmlfile 2>/dev/null


linkfile="`mktemp`"
perl parse_metacritic.pl $htmlfile > $linkfile

rm $htmlfile

infofile="`mktemp`"
for ((linec=1; linec<=`wc -l "$linkfile" | awk '{print $1}'`; linec++)); do 

	line="`head -$linec $linkfile | tail -1`"
	curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$line" > $htmlfile 2>/dev/null
	perl parse_metacritic_film.pl $htmlfile > $infofile

	FILM_TITLE="`head -1 $infofile`"
	FILM_SUMMARY="`head -2 $infofile | tail -1`"
	FILM_RELEASE_DATE="`head -3 $infofile | tail -1`"
	FILM_STUDIO="`head -4 $infofile | tail -1`"
	FILM_RATING="`head -5 $infofile | tail -1`"
	FILM_RATING_REASON="`head -6 $infofile | tail -1`"
	FILM_STARRING="`head -7 $infofile | tail -1`"
	FILM_GENRES="`head -8 $infofile | tail -1`"
	FILM_WRITERS="`head -9 $infofile | tail -1`"
	FILM_DIRECTOR="`head -10 $infofile | tail -1`"
	FILM_API_ID="`echo $FILM_RELEASE_DATE | sed 's/[^ 0-9]//g' | awk '{print $NF}'`-`echo $FILM_TITLE | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9 ]//g' | sed 's/ /-/g'`"

	MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
		"select movie_id from movies where api_id like '$FILM_API_ID'\G" |\
		grep movie_id | awk '{print $2}'`

	if [ "x$MOVIEID" = "x" -a "x$FILM_TITLE" != "x" ]; then

		echo "- $FILM_TITLE"
		echo "    TITLE=$FILM_TITLE"
		echo "    RELEASEDATE=$FILM_RELEASE_DATE"
		echo "    STUDIO=$FILM_STUDIO"
		echo "    RATING=$FILM_RATING"
		echo "    RATINGREASON=$FILM_RATING_REASON"

		# Add the movie to movies and get the movie id
		mysql -u $dbuser --password=$dbpass $dbname -e  \
			"insert into movies (title, release_date, rating, rating_reason, studio, api_id) values \
			('$FILM_TITLE', str_to_date('$FILM_RELEASE_DATE', '%M %e, %Y'), '$FILM_RATING', '$FILM_RATING_REASON', '$FILM_STUDIO', '$FILM_API_ID')"
		MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select movie_id from movies where api_id like '$FILM_API_ID'\G" |\
			grep movie_id | awk '{print $2}'`

	else
		echo "  $FILM_TITLE"
	fi

	# Make sure the movie has it's GD rating (thanks rottentomatoes)
	DBRATING=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select rating from movies where movie_id = $MOVIEID\G" |\
			grep rating | awk '{print $2}'`
	if [ \( "x$DBRATING" = "x" -o "x$DBRATING" = "xNULL" \) -a "x$FILM_RATING" != "x" ]; then
		echo "    RATING: $FILM_RATING"
		echo "    REASON: $FILM_RATING_REASON"
		mysql -u $dbuser --password=$dbpass $dbname -e  \
			"update movies set rating = '$FILM_RATING', rating_reason = '$FILM_RATING_REASON' where movie_id = $MOVIEID"
	fi

	# Make sure the movie has its release date
	DBRELEASEDATE=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select release_date from movies where movie_id = $MOVIEID\G" |\
			grep release_date | awk '{print $2}'`
	if [ \( "x$DBRELEASEDATE" = "x" -o "x$DBRELEASEDATE" = "xNULL" -o "x$DBRELEASEDATE" = "x0000-00-00" \) -a "x$FILM_RELEASE_DATE" != "x" ]; then
		echo "    RELEASEDATE: $FILM_RELEASE_DATE"
		mysql -u $dbuser --password=$dbpass $dbname -e  \
			"update movies set release_date = str_to_date('$FILM_RELEASE_DATE', '%M %e, %Y') where movie_id = $MOVIEID"
	fi


	# Add the movies summary
	EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e \
		"select 'true' is_there from movie_summaries where movie_id = $MOVIEID and source = 'metacritic'\G" |\
		grep is_there | head -1 | awk '{print $2}'`
	if [ "x$EXISTS" != "xtrue" ]; then
		FILM_SUMMARY=`echo $FILM_SUMMARY | sed 's/^ *//' | sed 's/ *$//'`
		if [ "x$FILM_SUMMARY" != "x" ]; then
			echo "    SUMMARY=`echo $FILM_SUMMARY | cut -c 1-50`..."
			mysql -u $dbuser --password=$dbpass $dbname -e \
				"insert into movie_summaries (movie_id, summary, source, link) values ($MOVIEID, '$FILM_SUMMARY', 'metacritic', '$line')"
		fi
	fi

	# Add the movie genres
	echo $FILM_GENRES | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' | sed 's/^ *//' | sed 's/ *$//' > $tmpfile
	for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do
		GENRE=`head -$i $tmpfile | tail -1`
		if [ "x$GENRE" = "x" ]; then continue; fi

		# Normalize the genres
		if [ "$GENRE" = "Comedies" ]; then GENRE="Comedy"; fi
		if [ "$GENRE" = "Dramas" ]; then GENRE="Dramas"; fi
		if [ "$GENRE" = "Science-Fiction" ]; then GENRE="Science Fiction"; fi
		if [ "$GENRE" = "Sci-fi" ]; then GENRE="Science Fiction"; fi
		if [ "$GENRE" = "Suspense" ]; then GENRE="Suspense/Thriller"; fi

		EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
				"select 'true' is_there from movie_genres where movie_id = $MOVIEID and genre = '$GENRE'\G" |\
				grep is_there | head -1 | awk '{print $2}'`
		if [ "x$EXISTS" != "xtrue" -a "x$GENRE" != "x" ]; then
			echo "    GENRE=$GENRE"
			mysql -u $dbuser --password=$dbpass $dbname -e \
				"insert into movie_genres (movie_id, genre) values ($MOVIEID, '$GENRE')"
		fi
	done

	# Add the movie stars
	echo $FILM_STARRING | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' | sed 's/^ *//' | sed 's/ *$//' > $tmpfile
	for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do
		STAR=`head -$i $tmpfile | tail -1`
		if [ "x$STAR" != "x" ]; then

			PREFIX=" "

			# Get the person's id
			PERSONID=`mysql -u $dbuser --password=$dbpass $dbname -e \
					"select mp_id from movie_people where name = '$STAR'\G" |\
					grep mp_id | awk '{print $2}'`
			if [ "x$PERSONID" = "x" ]; then
				PREFIX="+"
				mysql -u $dbuser --password=$dbpass $dbname -e \
					"insert into movie_people (name) values ('$STAR')"
				PERSONID=`mysql -u $dbuser --password=$dbpass $dbname -e \
					"select mp_id from movie_people where name = '$STAR'\G" |\
					grep mp_id | awk '{print $2}'`
			fi

			# Add the person to assoc table
			EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select 'true' is_there from movie_people_assoc where movie_id = $MOVIEID and mp_id = $PERSONID and role = 's'\G" |\
					grep is_there | head -1 | awk '{print $2}'`
			if [ "x$EXISTS" = "x" ]; then
				echo "    $PREFIX s $STAR"
				mysql -u $dbuser --password=$dbpass $dbname -e \
					"insert into movie_people_assoc (movie_id, mp_id, role) values ($MOVIEID, $PERSONID, 's')"
			fi
		fi
	done

	# Add the movie director
	echo $FILM_DIRECTOR | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' | sed 's/^ *//' | sed 's/ *$//' > $tmpfile
	for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do
		DIRECTOR=`head -$i $tmpfile | tail -1 | sed 's/^ *//' | sed 's/ *$//'`
		if [ "x$DIRECTOR" != "x" ]; then

			PREFIX=" "

			# Get the person's id
			PERSONID=`mysql -u $dbuser --password=$dbpass $dbname -e \
					"select mp_id from movie_people where name = '$DIRECTOR'\G" |\
					grep mp_id | awk '{print $2}'`
			if [ "x$PERSONID" = "x" ]; then
				PREFIX="+"
				mysql -u $dbuser --password=$dbpass $dbname -e \
					"insert into movie_people (name) values ('$DIRECTOR')"
				PERSONID=`mysql -u $dbuser --password=$dbpass $dbname -e \
					"select mp_id from movie_people where name = '$DIRECTOR'\G" |\
					grep mp_id | awk '{print $2}'`
			fi

			# Add the person to assoc table
			EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select 'true' is_there from movie_people_assoc where movie_id = $MOVIEID and mp_id = $PERSONID and role = 'd'\G" |\
					grep is_there | head -1 | awk '{print $2}'`
			if [ "x$EXISTS" = "x" ]; then
				echo "    $PREFIX d $DIRECTOR"
				mysql -u $dbuser --password=$dbpass $dbname -e \
					"insert into movie_people_assoc (movie_id, mp_id, role) values ($MOVIEID, $PERSONID, 'd')"
			fi
		fi
	done

	# Add the movie writers
	echo $FILM_WRITERS | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' | sed 's/^ *//' | sed 's/ *$//' > $tmpfile
	for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do
		WRITER=`head -$i $tmpfile | tail -1`
		if [ "x$WRITER" != "x" ]; then

			PREFIX=" "

			# Get the person's id
			PERSONID=`mysql -u $dbuser --password=$dbpass $dbname -e \
					"select mp_id from movie_people where name = '$WRITER'\G" |\
					grep mp_id | awk '{print $2}'`
			if [ "x$PERSONID" = "x" ]; then
				PREFIX="+"
				mysql -u $dbuser --password=$dbpass $dbname -e \
					"insert into movie_people (name) values ('$WRITER')"
				PERSONID=`mysql -u $dbuser --password=$dbpass $dbname -e \
					"select mp_id from movie_people where name = '$WRITER'\G" |\
					grep mp_id | awk '{print $2}'`
			fi

			# Add the person to assoc table
			EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select 'true' is_there from movie_people_assoc where movie_id = $MOVIEID and mp_id = $PERSONID and role = 'w'\G" |\
					grep is_there | head -1 | awk '{print $2}'`
			if [ "x$EXISTS" = "x" ]; then
				echo "    $PREFIX w $WRITER"
				mysql -u $dbuser --password=$dbpass $dbname -e \
					"insert into movie_people_assoc (movie_id, mp_id, role) values ($MOVIEID, $PERSONID, 'w')"
			fi
		fi
	done

	# Do the reviews
	for((i=11; i<=`wc -l $infofile | awk '{print $1'}`; i+=5)); do

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
		REVIEW_API_ID="`echo $REVIEW_NAME | sed 's/^ *//g' | sed 's/ *$//g' | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9 ]//g' | sed 's/ /-/g'`-at-`echo $REVIEW_PUBLISHER | sed 's/^ *//g' | sed 's/ *$//g' | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9 ]//g' | sed 's/ /-/g'`"

		CRITICID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select critic_id from critics where api_id = '$REVIEW_API_ID'\G" |\
			grep critic_id | awk '{print $2}'`
		newcritic=" "
			

		if [ "x$CRITICID" = "x" ]; then 

			mysql -u $dbuser --password=$dbpass $dbname -e \
				"insert into critics (name, publisher, api_id) values ('$REVIEW_NAME', '$REVIEW_PUBLISHER', '$REVIEW_API_ID')"
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
			mysql -u $dbuser --password=$dbpass $dbname -e \
				"insert into c_reviews (movie_id, critic_id, score, summary, link, source) \
				values ($MOVIEID, $CRITICID, $REVIEW_SCORE, '$REVIEW_SUMMARY', '$REVIEW_LINK', 'metacritic')"

		fi

	done
 
done

rm $linkfile
rm $infofile
rm $tmpfile

end=`date +%s`

minutestaken=`expr \( $end - $start \) / 60`

echo "===================================="
echo "==== total time : `printf "%3d" $minutestaken` minutes ======"
echo "===================================="
echo ""
echo ""
