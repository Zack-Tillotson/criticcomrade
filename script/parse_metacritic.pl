#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is http://www.metacritic.com/film/weekendboxoffice.shtml
# the output is a list of movie links


if(@ARGV == 0)
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
	print "Usage: 	perl parse_metacritic.pl <file name>\n";
	print "		file name = name of input file name which is http://www.metacritic.com/film/weekendboxoffice.shtml\n";
}

open(INFILE, $file) or die("Error: could not open input file - $file");

$atList = 0;
while($line = <INFILE>)
{

	chomp($line);
	
	if($atList == 0 && $line =~ /Wide Releases/)
	{
		$atList = 1;
	}

	if($atList == 1 && $line =~ /id="sortbyname1/)
	{
		$atList = 2;
	}

	if($atList == 2 && $line =~ /href="([^"]*)"/)
	{
		print "http://www.metacritic.com" . $1 . "\n";
	}

	if($atList == 2 && $line =~ /<\/p>/)
	{
		$atList = 3;
	}

	if($atList == 3 && $line =~ /Limited Releases/)
	{
		$atList = 4;
	}

	if($atList == 4 && $line =~ /id="sortbyname2/)
	{
		$atList = 5;
	}

	if($atList == 5 && $line =~ /href="([^"]*)"/)
	{
		print "http://www.metacritic.com" . $1 . "\n";
	}

	if($atList == 5 && $line =~ /<\/p>/)
	{
		$atList = 6;
	}

}

close(INFILE);
