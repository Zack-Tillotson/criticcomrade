#!/bin/bash 
# This script will get a movie's info from IMDB and add it to the database. It takes one argument - a link to a movie's page on IMDB.  

site="http://www.imdb.com/title/tt1136608/" 
dbname="critic_review_new" 
dbuser="critic_review" 
dbpass="critic_review_pwd" 
picturelocation="/var/www/criticcomrade/common/img/movie" 

if [ "x$@" != "x" ]; then site="$@"; fi 

htmlfile="`mktemp`" 
infofile="`mktemp`" 
picfile="`mktemp`"
tmpfile="`mktemp`"

curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$site" > $htmlfile 2>/dev/null

perl parse_imdb_getMovie.pl $htmlfile > $infofile

MOVIE_TITLE="`head -1 $infofile`"
MOVIE_SUMMARY="`head -2 $infofile | tail -1`"
MOVIE_RELEASE_DATE="`head -3 $infofile | tail -1`"
if [ "$MOVIE_RELEASE_DATE" = "" ]; then MOVIE_RELEASE_DATE=`date "+%B %d, %Y"`; fi
MOVIE_RATING="`head -4 $infofile | tail -1`"
MOVIE_RATING_REASON="`head -5 $infofile | tail -1`"
MOVIE_GENRES="`head -6 $infofile | tail -1`"
MOVIE_WRITERS="`head -7 $infofile | tail -1`"
MOVIE_DIRECTOR="`head -8 $infofile | tail -1`"
MOVIE_PIC_LINK="`head -9 $infofile | tail -1`"
MOVIE_API_ID="`echo $MOVIE_RELEASE_DATE | sed 's/[^ 0-9]//g' | awk '{print $NF}'`-`echo $MOVIE_TITLE | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9 ]//g' | sed 's/ /-/g'`"


if [ "$MOVIE_API_ID" = "-" ]; then exit 1; fi
if [ "$MOVIE_RELEASE_DATE" = "" ]; then MOVIE_RELEASE_DATE=`date "+%B %d, %Y"`; fi

# Ensure the movie is in movies
MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
		"select movie_id from movies where api_id = '$MOVIE_API_ID'\G" |\
		grep movie_id | head -1 | awk '{print $2}'`

if [ "x$MOVIEID" = "x" ]; then

	echo "- $MOVIE_TITLE"
	echo "    SITE=$site"
	echo "    RELEASEDATE=$MOVIE_RELEASE_DATE"
	echo "    RATING=$MOVIE_RATING"
	echo "    RATINGREASON=$MOVIE_RATING_REASON"
	echo "    STUDIO=$MOVIE_STUDIO"

	mysql -u $dbuser --password=$dbpass $dbname -e  \
		"insert into movies (title, release_date, rating, rating_reason, studio, api_id) values \
		('$MOVIE_TITLE', str_to_date('$MOVIE_RELEASE_DATE', '%e %M %Y'), '$MOVIE_RATING', '$MOVIE_RATING_REASON', '$MOVIE_STUDIO', '$MOVIE_API_ID')"
	MOVIEID=`mysql -u $dbuser --password=$dbpass $dbname -e  \
		"select movie_id from movies where title like '$MOVIE_TITLE'\G" |\
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

# Ensure the summary are in movie_summaries
EXISTS=`mysql -u $dbuser --password=$dbpass $dbname -e  \
		"select 'true' is_there from movie_summaries where movie_id = $MOVIEID and source = 'imdb'\G" |\
		grep is_there | head -1 | awk '{print $2}'`
if [ "x$EXISTS" != "xtrue" ]; then
	MOVIE_SUMMARY=`echo $MOVIE_SUMMARY | sed 's/^ *//' | sed 's/ *$//'`
	if [ "x$MOVIE_SUMMARY" != "x" ]; then
		echo "    SUMMARY=`echo $MOVIE_SUMMARY | cut -c 1-50`..."
		mysql -u $dbuser --password=$dbpass $dbname -e  \
			"insert into movie_summaries (movie_id, summary, link, source) values \
			 ($MOVIEID, '$MOVIE_SUMMARY', '$site', 'imdb')"
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
	if [ "$GENRE" = "Sci-Fi" ]; then GENRE="Science Fiction"; fi
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

# Ensure the stars are in movie_people and movie_people_assoc
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

# Get the picture for this movie and movie it to the correct location
curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$MOVIE_PIC_LINK" > $picfile 2>/dev/null
if [ ! -f $picturelocation/"$MOVIE_API_ID"_poster.jpg ]; then 
	echo "Adding picture: $picturelocation/"$MOVIE_API_ID"_poster.jpg"
	chmod a+rw $picfile
	cp $picfile $picturelocation/"$MOVIE_API_ID"_poster.jpg
fi

rm $htmlfile
rm $infofile
rm $picfile
rm $tmpfile
