#!/bin/bash

start=`date +%s`

echo "======================================="
echo "=== starting rottentomatoes scratch ==="
echo "===   `date`  ==="
echo "======================================="

site="http://www.rottentomatoes.com/movies/in_theaters.php?sortby=title&mode=simple&order=ASC&=&page=1"
dbname="critic_review_new"
dbuser="critic_review"
dbpass="critic_review_pwd"

if [ "x$@" != "x" ]; then site="$@"; fi

htmlfile="`mktemp`"
linkfile="`mktemp`"
movielinkfile="`mktemp`"
reviewlinkfile="`mktemp`"
infofile="`mktemp`"
queryfile="`mktemp`"
tmpfile="`mktemp`"

curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$site" > $htmlfile 2>/dev/null

perl parse_rt_getMovieLinksPages.pl $htmlfile > $linkfile

# Add the openings page, because they aren't included in the 'in theaters' page
echo 'http://www.rottentomatoes.com/movies/opening.php?sortby=title&mode=simple&order=ASC&=&page=1' >> $linkfile

for((linkc=1; linkc<=`wc -l $linkfile | awk '{print $1}'`; linkc++)); do

	link="`head -$linkc "$linkfile" | tail -1`"
	curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$link" > $htmlfile 2>/dev/null

	perl parse_rt_getMovieLinks.pl $htmlfile > $movielinkfile

	for((linkd=1; linkd<=`wc -l $movielinkfile | awk '{print $1}'`; linkd++)); do

		movielink="`head -$linkd $movielinkfile | tail -1`"
		curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$movielink" > $htmlfile 2>/dev/null

		movielinknoparam="`echo $movielink | awk -F'?' '{print $1}'`"

		perl parse_rt_getReviewsPages.pl "$htmlfile" "$movielinknoparam" > "$reviewlinkfile"

		for((linke=1; linke<=`wc -l $reviewlinkfile | awk '{print $1}'`; linke++)); do


			reviewlink="`head -$linke $reviewlinkfile | tail -1`"
			curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$reviewlink" > $htmlfile 2>/dev/null
			perl parse_rt_getReviews.pl "$htmlfile" > "$infofile"

			MOVIE_TITLE="`head -1 $infofile`"
			MOVIE_SHORT_SUMMARY="`head -2 $infofile | tail -1`"
			MOVIE_SUMMARY="`head -3 $infofile | tail -1`"
			MOVIE_RELEASE_DATE="`head -4 $infofile | tail -1`"
			MOVIE_STUDIO="`head -5 $infofile | tail -1`"
			MOVIE_RATING="`head -6 $infofile | tail -1`"
			MOVIE_RATING_REASON="`head -7 $infofile | tail -1`"
			MOVIE_STARS="`head -8 $infofile | tail -1`"
			MOVIE_GENRES="`head -9 $infofile | tail -1`"
			MOVIE_WRITERS="`head -10 $infofile | tail -1`"
			MOVIE_DIRECTOR="`head -11 $infofile | tail -1`"
			MOVIE_API_ID="`echo $MOVIE_RELEASE_DATE | sed 's/[^ 0-9]//g' | awk '{print $NF}'`-`echo $MOVIE_TITLE | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9 ]//g' | sed 's/ /-/g'`"

			# Ensure the movie is in movies
			MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select movie_id from movies where api_id like '$MOVIE_API_ID'\G" |\
					grep movie_id | head -1 | awk '{print $2}'`
			if [ "x$MOVIEID" = "x" ]; then

				echo "- $MOVIE_TITLE"
				echo "    SITE=$movielinknoparam"
				echo "    RELEASEDATE=$MOVIE_RELEASE_DATE"
				echo "    RATING=$MOVIE_RATING"
				echo "    RATINGREASON=$MOVIE_RATING_REASON"
				echo "    STUDIO=$MOVIE_STUDIO"

				mysql -u $dbuser --password=$dbpass $dbname -e  \
					"insert into movies (title, release_date, rating, rating_reason, studio, api_id) values \
					('$MOVIE_TITLE', str_to_date('$MOVIE_RELEASE_DATE', '%M %e, %Y'), '$MOVIE_RATING', '$MOVIE_RATING_REASON', '$MOVIE_STUDIO', '$MOVIE_API_ID')"
				MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select movie_id from movies where api_id like '$MOVIE_API_ID'\G" |\
					grep movie_id | head -1 | awk '{print $2}'`

			else
				echo "  $MOVIE_TITLE"
			fi

			# Make sure the movie has it's GD rating (thanks rottentomatoes)
			DBRATING=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select rating from movies where movie_id = $MOVIEID\G" |\
					grep rating | awk '{print $2}'`
			if [ \( "x$DBRATING" = "x" -o "x$DBRATING" = "xNULL" \) -a "x$MOVIE_RATING" != "x" ]; then
				echo "    RATING: $MOVIE_RATING"
				echo "    REASON: $MOVIE_RATING_REASON"
				mysql -u $dbuser --password=$dbpass $dbname -e  \
					"update movies set rating = '$MOVIE_RATING', rating_reason = '$MOVIE_RATING_REASON' where movie_id = $MOVIEID"
			fi

			# Make sure the movie has its release date
			DBRELEASEDATE=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select release_date from movies where movie_id = $MOVIEID\G" |\
					grep release_date | awk '{print $2}'`
			if [ \( "x$DBRELEASEDATE" = "x" -o "x$DBRELEASEDATE" = "xNULL" -o "x$DBRELEASEDATE" = "x0000-00-00" \) -a "x$MOVIE_RELEASE_DATE" != "x" ]; then
				echo "    RELEASEDATE: $MOVIE_RELEASE_DATE"
				mysql -u $dbuser --password=$dbpass $dbname -e  \
					"update movies set release_date = str_to_date('$MOVIE_RELEASE_DATE', '%M %e, %Y') where movie_id = $MOVIEID"
			fi


			# Ensure the summaries are in movie_summaries
			EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select 'true' is_there from movie_summaries where movie_id = $MOVIEID and source = 'rottentomatoes-blurb'\G" |\
					grep is_there | head -1 | awk '{print $2}'`
			if [ "x$EXISTS" != "xtrue" ]; then
				MOVIE_SHORT_SUMMARY=`echo $MOVIE_SHORT_SUMMARY | sed 's/^ *//' | sed 's/ *$//'`
				if [ "x$MOVIE_SHORT_SUMMARY" != "x" ]; then
					echo "    SHORTSUMMARY=`echo $MOVIE_SHORT_SUMMARY | cut -c 1-50`"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_summaries (movie_id, summary, link, source) values \
						 ($MOVIEID, '$MOVIE_SHORT_SUMMARY', '$movielinknoparam', 'rottentomatoes-blurb')"
				fi
			fi

			EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select 'true' is_there from movie_summaries where movie_id = $MOVIEID and source = 'rottentomatoes-full'\G" |\
					grep is_there | head -1 | awk '{print $2}'`
			if [ "x$EXISTS" != "xtrue" ]; then
				MOVIE_SUMMARY=`echo $MOVIE_SUMMARY | sed 's/^ *//' | sed 's/ *$//'`
				if [ "x$MOVIE_SUMMARY" != "x" ]; then
					echo "    SUMMARY=`echo $MOVIE_SUMMARY | cut -c 1-50`..."
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_summaries (movie_id, summary, link, source) values \
						 ($MOVIEID, '$MOVIE_SUMMARY', '$movielinknoparam', 'rottentomatoes-full')"
				fi
			fi

			# Ensure the genres are in movie_genres
			echo $MOVIE_GENRES | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' > $tmpfile
			for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do

				GENRE=`head -$i $tmpfile | tail -1 | sed 's/^ *//' | sed 's/ *$//'`
				if [ "x$GENRE" = "x" ]; then continue; fi
			
				# Normalize the genres	
				if [ "$GENRE" = "Comedies" ]; then GENRE="Comedy"; fi
				if [ "$GENRE" = "Dramas" ]; then GENRE="Drama"; fi
				if [ "$GENRE" = "Science-Fiction" ]; then GENRE="Science Fiction"; fi
				if [ "$GENRE" = "Sci-fi" ]; then GENRE="Science Fiction"; fi
				if [ "$GENRE" = "Suspense" ]; then GENRE="Suspense/Thriller"; fi
				if [ "`echo $GENRE | awk '{print $1}'`" = "Musical" ]; then GENRE="Musical"; fi

				EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
						"select 'true' is_there from movie_genres where movie_id = $MOVIEID and genre = '$GENRE'\G" |\
						grep is_there | head -1 | awk '{print $2}'`
				if [ "x$EXISTS" != "xtrue" ]; then
					echo "    GENRE=$GENRE"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_genres (movie_id, genre) values ($MOVIEID, '$GENRE')"
				fi
			done

			# Ensure the people are in movie_people and movie_people_assoc
			echo $MOVIE_STARS | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' > $tmpfile
			for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do
				STAR=`head -$i $tmpfile | tail -1 | sed 's/^ *//' | sed 's/ *$//'`
				PEOPLEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
						"select mp_id from movie_people where name = '$STAR'\G" |\
						grep mp_id | head -1 | awk '{print $2}'`
				PREFIX=" "
				if [ "x$PEOPLEID" = "x" ]; then
					PREFIX="+"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_people (name) values ('$STAR')"
					PEOPLEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
							"select mp_id from movie_people where name = '$STAR'\G" |\
							grep mp_id | head -1 | awk '{print $2}'`
				fi
				EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
						"select 'true' is_there from movie_people_assoc where movie_id = $MOVIEID and mp_id = $PEOPLEID and role = 's'\G" |\
						grep is_there | head -1 | awk '{print $2}'`
				if [ "x$EXISTS" = "x" ]; then
					echo "    $PREFIX s $STAR"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_people_assoc (movie_id, mp_id, role) values ($MOVIEID, $PEOPLEID, 's')"
				fi
			done

			# Add the writers to movie_people and movie_people_assoc
			echo $MOVIE_WRITERS | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' > $tmpfile
			for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do
				WRITER=`head -$i $tmpfile | tail -1 | sed 's/^ *//' | sed 's/ *$//'`
				PEOPLEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
						"select mp_id from movie_people where name = '$WRITER'\G" |\
						grep mp_id | head -1 | awk '{print $2}'`
				PREFIX=" "
				if [ "x$PEOPLEID" = "x" ]; then
					PREFIX="+"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_people (name) values ('$WRITER')"
					PEOPLEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
							"select mp_id from movie_people where name = '$WRITER'\G" |\
							grep mp_id | head -1 | awk '{print $2}'`
				fi
				EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
						"select 'true' is_there from movie_people_assoc where movie_id = $MOVIEID and mp_id = $PEOPLEID and role = 'w'\G" |\
						grep is_there | head -1 | awk '{print $2}'`
				if [ "x$EXISTS" = "x" ]; then
					echo "    $PREFIX w $WRITER"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_people_assoc (movie_id, mp_id, role) values ($MOVIEID, $PEOPLEID, 'w')"
				fi
			done

			# Add the director to movie_people and movie_people_assoc
			echo $MOVIE_DIRECTOR | awk -F',' '{for(i=1; i<=NF; i++) { print $i }}' > $tmpfile
			for((i=1; i<=`wc -l $tmpfile | awk '{print $1}'`; i++)); do
				DIRECTOR=`head -$i $tmpfile | tail -1 | sed 's/^ *//' | sed 's/ *$//'`
				PEOPLEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
						"select mp_id from movie_people where name = '$DIRECTOR'\G" |\
						grep mp_id | head -1 | awk '{print $2}'`
				PREFIX=" "
				if [ "x$PEOPLEID" = "x" ]; then
					PREFIX="+"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_people (name) values ('$DIRECTOR')"
					PEOPLEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
							"select mp_id from movie_people where name = '$DIRECTOR'\G" |\
							grep mp_id | head -1 | awk '{print $2}'`
				fi
				EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
						"select 'true' is_there from movie_people_assoc where movie_id = $MOVIEID and mp_id = $PEOPLEID and role = 'd'\G" |\
						grep is_there | head -1 | awk '{print $2}'`
				if [ "x$EXISTS" = "x" ]; then
					echo "    $PREFIX d $DIRECTOR"
					mysql -u $dbuser --password=$dbpass $dbname -e  \
						"insert into movie_people_assoc (movie_id, mp_id, role) values ($MOVIEID, $PEOPLEID, 'd')"
				fi
			done

			for((i=12; i<=`wc -l $infofile | awk '{print $1'}`; i+=6)); do

				a=$i;
				b=`expr $a + 1`
				c=`expr $b + 1`
				d=`expr $c + 1`
				e=`expr $d + 1`
				f=`expr $e + 1`

				REVIEW_NAME="`head -$a $infofile | tail -1`"
				REVIEW_PUBLISHER="`head -$b $infofile | tail -1`"
				REVIEW_SUMMARY="`head -$c $infofile | tail -1`"
				REVIEW_LINK="`head -$d $infofile | tail -1`"
				REVIEW_SCORE="`head -$e $infofile | tail -1`"
				REVIEW_DATE="`head -$f $infofile | tail -1`"
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

					curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" -D $htmlfile "http://www.rottentomatoes.com$REVIEW_LINK" 1>/dev/null 2>/dev/null
					REVIEW_REAL_LINK="`grep Location $htmlfile | awk '{print $2}' | sed "s/'/\\\\\\\\'/g"`"

					if [ "x$REVIEW_REAL_LINK" = "x" -o "x$REVIEW_REAL_LINK" = "x0" ]; then REVIEW_REAL_LINK="http://www.rottentomatoes.com$REVIEWLINK"; fi

					echo "   $newcritic $REVIEW_SCORE $REVIEW_NAME ($CRITICID)"
					mysql -u $dbuser --password="$dbpass" $dbname -e \
						"insert into c_reviews (movie_id, critic_id, score, summary, link, review_date, source) \
						values ($MOVIEID, $CRITICID, $REVIEW_SCORE, '$REVIEW_SUMMARY', '$REVIEW_REAL_LINK', \
						str_to_date('$REVIEW_DATE', '%b %d %Y'), 'rottentomatoes')"

				fi

			done

		done

	done

done

rm "$htmlfile"
rm "$linkfile"
rm "$movielinkfile"
rm "$reviewlinkfile"
rm "$infofile"

end=`date +%s`

minutestaken=`expr \( $end - $start \) / 60`

echo "===================================="
echo "==== total time : `printf "%3d" $minutestaken` minutes ======"
echo "===================================="
echo ""
echo ""
