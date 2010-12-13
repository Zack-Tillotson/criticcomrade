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

file="`mktemp`"
w3m -dump_source "$site" > $file

closetolist=0;
atlist=0;
for ((line=1; line<=`wc -l "$file" | awk '{print $1}'`; line++)); do 

	if [ "x`head -$line $file | tail -1 | grep "Wide Releases" | wc | awk '{print $3}'`" != "x0" ]; then 
		closetolist=1;
	fi
	
	if [ "x`head -$line $file | tail -1 | grep "listing" | wc | awk '{print $3}'`" != "x0" -a $closetolist -eq 1 ]; then 
		atlist=1;
	fi
		

	if [ $atlist -eq 1 -a "x`head -$line $file | tail -1 | grep HREF | wc | awk '{print $3}'`" != "x0" ]; then 

		FILMNAME="`head -$line $file | tail -1 | awk -F'>' '{print $2}' | awk -F'<' '{print $1}'`"
		FILMLINK="`head -$line $file | tail -1 | awk -F'"' '{print $2}'`"
		
		if [ "x`head -$line $file | tail -1 | grep "IMG SRC=" | wc | awk '{print $3}'`" != "x0" ]; then 
			FILMNAME="`head -$line $file | tail -1 | awk -F'>' '{print $3}' | awk -F'<' '{print $1}'`"
		fi
	
		foundmovie=`mysql -u $dbuser --password="$dbpass" $dbname -e  \
			   "select movie_id from movies where title='$FILMNAME'\G" | grep movie_id |\
			    wc -l | awk '{print $1}'`
	
		newmovie=" "	
		if [ $foundmovie -eq 0 ]; then
			newmovie="-"
		fi

		echo "$newmovie $FILMNAME"
		~/Documents/scripts/parse_metacritic_film.sh "$FILMNAME" "http://www.metacritic.com$FILMLINK"

		line=`expr $line + 1`
	fi 

	# at the end of the movie list, stop parsing the file	
	if [ "x`head -$line $file | tail -1 | grep "</p>" | wc | awk '{print $3}'`" != "x0" -a $atlist -eq 1 ]; then 
		line=`wc -l $file | awk '{print $1}'`
	fi

done

end=`date +%s`

minutestaken=`expr \( $end - $start \) / 60`

echo "===================================="
echo "==== total time : `printf "%3d" $minutestaken` minutes ======"
echo "===================================="
echo ""
echo ""

rm "$file"
