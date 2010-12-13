#!/bin/bash

start=`date +%s`

echo "======================================="
echo "===      starting imdb scratch      ==="
echo "===   `date`  ==="
echo "======================================="

site1="http://www.imdb.com/chart/"
site2="http://www.imdb.com/nowplaying/"
dbname="critic_review_new"
dbuser="critic_review"
dbpass="critic_review_pwd"

htmlfile="`mktemp`"
linkfile="`mktemp`"

# First get the Top 10 info
curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$site1" > $htmlfile 2>/dev/null

# This file contains the top 10 movies, in the form
#	chart date 	# The date this chart is of
#	title		# movie 1
#	year
#	link
#	rank
#	title		# movie 2
#	year
#	link
#	rank
#	...		# up to 10 movies...
perl parse_imdb_top10.pl $htmlfile > $linkfile

chartdate=`head -1 "$linkfile"`

for((linkc=2; linkc<=`wc -l $linkfile | awk '{print $1}'`; linkc+=4)); do

	plusone=`expr $linkc + 1`
	plustwo=`expr $linkc + 2`
	plusthree=`expr $linkc + 3`

	title="`head -$linkc "$linkfile" | tail -1`"
	year="`head -$plusone "$linkfile" | tail -1`"
 	link="`head -$plustwo "$linkfile" | tail -1`"
	rank="`head -$plusthree "$linkfile" | tail -1`"
	apiid="`echo $year | sed 's/[^ 0-9]//g'`-`echo $title | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9 ]//g' | sed 's/ /-/g'`"

	# get movie id, add it if it's not in db already
	movieid=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select movie_id from movies where api_id = '$apiid'\G" |\
			grep movie_id | awk '{print $2}'`

	if [ "$movieid" = "" ]; then
		`echo $0 | sed 's/\/[^\/]*$//'`/scrape_imdb_movie.sh "$link"
		movieid=`mysql -u $dbuser --password=$dbpass $dbname -e  \
				"select movie_id from movies where api_id = '$apiid'\G" |\
				grep movie_id | awk '{print $2}'`
		if [ "$movieid" = "" ]; then
			year=`date +%Y`
			apiid="`echo $year | sed 's/[^ 0-9]//g'`-`echo $title | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9 ]//g' | sed 's/ /-/g'`"
			movieid=`mysql -u $dbuser --password=$dbpass $dbname -e  \
					"select movie_id from movies where api_id = '$apiid'\G" |\
					grep movie_id | awk '{print $2}'`
		fi
	fi

	week=`date -d "$chartdate" +%U`
	chartyear=`date -d "$chartdate" +%Y`

	# add to rank table, if not there yet
	isinrankings=`mysql -u $dbuser --password=$dbpass $dbname -e  \
			"select 'true' in_rankings from movie_rankings where movie_id = $movieid and week = $week and year = $chartyear\G" |\
			grep in_rankings | awk '{print $2}'`

	if [ "$isinrankings" != "true" ]; then
		mysql -u $dbuser --password=$dbpass $dbname -e  \
			"insert into movie_rankings (movie_id, week, rank, year) values ($movieid, $week, $rank, $chartyear)"
	fi

done

# Now get the upcoming movies
curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13" "$site2" > $htmlfile 2>/dev/null

# This file contains links to movie pages
perl parse_imdb_upcoming.pl $htmlfile > $linkfile

for((linkc=1; linkc<=`wc -l $linkfile | awk '{print $1}'`; linkc++)); do

	link="`head -$linkc "$linkfile" | tail -1`"
	`echo $0 | sed 's/\/[^\/]*$//'`/scrape_imdb_movie.sh "$link"

done

# Now get the stars of the current movies -- not done yet

rm "$htmlfile"
rm "$linkfile"

end=`date +%s`

minutestaken=`expr \( $end - $start \) / 60`

echo "===================================="
echo "==== total time : `printf "%3d" $minutestaken` minutes ======"
echo "===================================="
echo ""
echo ""
