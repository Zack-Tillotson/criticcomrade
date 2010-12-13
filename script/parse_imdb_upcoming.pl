#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is http://www.imdb.com/nowplaying/
# the output is the movie rankings in this format:
# 	Movie link
# 	etc...


if(@ARGV == 0 || @ARGV > 1)
{
	usage();
	exit();
}

my $i = 0;
my $file = "";
foreach my $parm (@ARGV)
{
	$file = $parm;
	$i++;
}

if($i > 1)
{
	usage();
	exit();
}

sub usage
{
	print "Usage: 	perl parse_imdb_upcoming.pl <file name>\n";
	print "		file name = name of input file name which is http://www.imdb.com/nowplaying/"
}

open(INFILE, $file) or die("Error: could not open input file - $file");

$atList = 0;
while($line = <INFILE>)
{


	chomp($line);

	if($line =~ /<h3>Coming Soon<\/h3>/) { $atList = 5; }

	# The week this is for
	if( $atList == 5 && $line =~ /<li>/)
	{

		@links = split(/href=/, $line);
		foreach(@links)
		{
			$_ =~ s/^.*"([^"]*)".*/$1/;
			if($_ =~ /title/)
			{
				print "http://www.imdb.com" . $_ . "\n";
			}
		}

		$line = 6;

	}

	# Done
	if($atList == 6 && $line =~ /<\/div>/)
	{
		last;
	}

}

close(INFILE);
