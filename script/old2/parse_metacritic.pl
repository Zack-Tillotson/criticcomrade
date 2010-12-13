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
	
	if($atList == 0 && $line =~ /The 20 Highest Grossing/)
	{
		$atList = 1;
	}

	if($atList > 0 && $atList < 3 && $line =~ /href/)
	{

		@words = split(/"/, $line);

		$link = $words[3];

		print "http://www.metacritic.com" . $link . "\n";

	}
	
	if($atList > 0 && $line =~ /tbody/)
	{

		$atList++;

	}

}

close(INFILE);
