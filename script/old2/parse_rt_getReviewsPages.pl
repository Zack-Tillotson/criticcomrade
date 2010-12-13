# This program is called with 2 inputs - the input file and the link to the movie
# the input file is like http://www.rottentomatoes.com/m/pray_the_devil_back_to_hell/?page=1&critic=columns&sortby=date&name_order=asc&view=text#contentReviews
# the input link is like http://www.rottentomatoes.com/m/pray_the_devil_back_to_hell/
# the output is a list of pages that have reviews on them


if(@ARGV == 0)
{
	usage();
	exit();
}

my $i = 0;
my $file = "";
my $link = "";

if(@ARGV != 2)
{
	usage();
	exit();
}

$file = $ARGV[0];
$link = $ARGV[1];

sub usage
{
	print "Usage: 	perl parse_rt_getReviewsPages.pl <file name> <movie link>\n";
	print "		file name = name of input file name which is http://www.metacritic.com/film/weekendboxoffice.shtml\n";
	print "		movie link = link to the rottentomatoes page which is stored in <file name>, sans url parameters\n";
}

open(INFILE, $file) or die("Error: could not open input file - $file");

$atList = 0;
while($line = <INFILE>)
{

	chomp($line);
	
	if($atList == 0 && $line =~ /Bubble View/)
	{
		$atList = 1;
	}

	if($atList == 1 && $line =~ /href="\?page=(.?.?)&critic=approved&sortby=date&name_order=asc&view=text#contentReviews">&gt;\|<\/a>/)
	{

		$atList = 2;

		for($i = 1; $i <= $1; $i++)
		{

			print $link . "?page=" . $i . "&critic=approved&sortby=date&name_order=asc&view=text#contentReviews\n";

		}

	}
	
	if($atList == 1 && $line =~ /<\/div>/)
	{

		$atList = 2;

		print $link . "?page=1&critic=approved&sortby=date&name_order=asc&view=text#contentReviews\n";

	}

}

close(INFILE);
