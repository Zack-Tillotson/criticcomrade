#!/bin/bash

MOVIETITLE="Shine A Light"
SITE="http://www.metacritic.com/video/titles/shinealight"

if [ "x$1" != "x" ]; then MOVIETITLE="$1"; fi
if [ "x$2" != "x" ]; then SITE="$2"; fi

file="`mktemp`"
w3m -dump_source "$SITE" > $file

gettingsummary=0
addmovie=0

for ((line=0; line<`wc -l $file | awk '{print $1}'`; line++)); do 

	if [ $gettingsummary -ne 3 ]; then

		if [ $gettingsummary -eq 0 -a "x`head -$line $file | tail -1 | grep "<p>Starring" | wc | awk '{print $3}'`" != "x0" ]; then 
			gettingsummary=1
		fi

		if [ $gettingsummary -eq 1 -a "x`head -$line $file | tail -1 | grep "</p>" | wc | awk '{print $3}'`" != "x0" ]; then 
			gettingsummary=2
		fi
	
		if [ $gettingsummary -eq 2 -a "x`head -$line $file | tail -1 | grep "<p>" | wc | awk '{print $3}'`" != "x0" ]; then 

			gettingsummary=3

			MOVIESUMMARY="`head -$line $file | tail -1 | awk -F'>' '{print $2}' | awk -F'<' '{print $1}' | sed 's/\x27/\\\x27/g'`"

		fi

	fi
	
	if [ "x`head -$line $file | tail -1 | grep "Theatrical: <b>" | wc | awk '{print $3}'`" != "x0" ]; then 

		RELEASEDATE=`head -$line $file | tail -1 | awk -F'>' '{print $2}' | awk -F'<' '{print $1}'`
		month=`echo $RELEASEDATE | awk '{print $1}'`
		day=`echo $RELEASEDATE | awk '{print $2}' | sed 's/,//'`
		year=`echo $RELEASEDATE | awk '{print $3}'`
		RELEASEDATE="$month `printf \"%02d\" $day` $year"
		RELEASEDATE="`date -d \"$RELEASEDATE\" \"+%Y-%m-%d 00:00:00\"`"

		addmovie=1

	fi

	if [ $addmovie -eq 1 ]; then

		MOVIEID=`mysql -u critic_review --password="critic_review_pwd" critic_review -e  \
			"select movie_id from movies where title like '$MOVIETITLE'\G" |\
			grep movie_id | awk '{print $2}'`

		if [ "x$MOVIEID" == "x" ]; then

			# Add this movie to the DB
			mysql -u critic_review --password="critic_review_pwd" critic_review -e  \
				"insert into movies (title, summary, link, release_date) values \
				('$MOVIETITLE', '$MOVIESUMMARY', '$SITE', '$RELEASEDATE')"
			MOVIEID=`mysql -u critic_review --password="critic_review_pwd" critic_review -e  \
				"select movie_id from movies where title like '$MOVIETITLE'\G" |\
				grep movie_id | awk '{print $2}'`

			echo "    TITLE=$MOVIETITLE"
			echo "    SITE=$SITE"
			echo "    SUMMARY=`echo "$MOVIESUMMARY" | cut -c 1-50`..."
			echo "    RELEASEDATE=$RELEASEDATE"

		fi

		addmovie=2

	fi
		
	if [ "x`head -$line $file | tail -1 | grep criticreview | wc | awk '{print $3}'`" != "x0" ]; then 

		publine=`expr $line + 1`

		nameline=0
		for ((i=`expr $publine + 1`; nameline<1; i++)); do
			if [ "x`head -$i $file | tail -1 | wc | awk '{print $2}'`" != "x0" ]; then
				nameline=$i
			fi
		done

		summaryline=0
		for ((i=`expr $nameline + 1`; summaryline<1; i++)); do
			if [ "x`head -$i $file | tail -1 | grep 'class="quote"' | wc | awk '{print $2}'`" != "x0" ]; then
				summaryline=$i
			fi
		done

		linkline=`expr $summaryline + 1`

		scoreline=`expr $line - 1`

		CRITICPUB="`head -$publine $file | tail -1 | awk -F'>' '{print $3}' | awk -F'<' '{print $1}'`"
		CRITICNAME="`head -$nameline $file | tail -1 | awk -F'>' '{print $2}' | awk -F'<' '{print $1}'`"
		CRITICSUMMARY="`head -$summaryline $file | tail -1 | awk -F'>' '{print $2}' | awk -F'<' '{print $1}' | sed 's/\x27/\\\x27/g'`"
		CRITICLINK="`head -$linkline $file | tail -1 | awk -F'"' '{print $2}' | awk -F'"' '{print $1}'`"
		CRITICSCORE="`head -$scoreline $file | tail -1 | awk -F'>' '{print $3}' | awk -F'<' '{print $1}'`"

		CRITICID=`mysql -u critic_review --password="critic_review_pwd" critic_review -e  \
			"select critic_id from critics where name like '$CRITICNAME' and publisher like '$CRITICPUB'\G" |\
			grep critic_id | awk '{print $2}'`

		newcritic=" "
		if [ "x$CRITICID" == "x" ]; then
			newcritic="-"
			mysql -u critic_review --password="critic_review_pwd" critic_review -e \
				"insert into critics (name, publisher, link) values ('$CRITICNAME', '$CRITICPUB', '$CRITICLINK');"
			CRITICID=`mysql -u critic_review --password="critic_review_pwd" critic_review -e  \
				"select critic_id from critics where name like '$CRITICNAME' and publisher like '$CRITICPUB'\G" |\
				grep critic_id | awk '{print $2}'`
		fi

		reviewindb=`mysql -u critic_review --password="critic_review_pwd" critic_review -e  \
			   "select review_id from c_reviews where critic_id = $CRITICID and movie_id = $MOVIEID \G" |\
			   grep review_id | wc -l | awk '{print $1}'`

		if [ $reviewindb -eq 0 ]; then
		
			echo "   $newcritic $CRITICSCORE $CRITICNAME ($CRITICID)" 

			mysql -u critic_review --password="critic_review_pwd" critic_review -e \
				"insert into c_reviews (movie_id, critic_id, score, summary, link) \
				values ($MOVIEID, $CRITICID, $CRITICSCORE, '$CRITICSUMMARY', '$CRITICLINK')"

		fi

		line=`expr $i + 1`
	fi 
	
done

rm "$file"
