#!/usr/bin/perl

require './copy_site.pm';

my $sftp = bgcrm_connect();

my @ls = @{ $sftp->ls( "/var/ftp/pub/bgerp/$VERSION/", names_only => 1)
	or die "Unable to retrieve directory: ".$sftp->error };

my $branch = `git branch | grep "*"`;
$branch =~ s/\*//;
chomp( $branch );

#print( substr( $branch, "master" ) );

if( $branch != 'master' )
{
	die "Can't publish on $branch branch.\n";
}

# версии пакетов на сервере
my %remote_data = {};

print( "Taking remote data:\n" );
foreach my $file ( @ls )
{
	if( $file =~ /^\.+/ )
	{
		next;
	}	

	my @name_build = get_name_build( $file );
	my $name = $name_build[0];
	my $build = $name_build[1];

	$remote_data{$name} = [$build, $file];

	print( "Remote $name - $build - $file\n" );
}

print( "\nSync:\n" );

my $was_copy = undef;

dir_sync( "bgerp" );
dir_sync( "update" );
dir_sync( "update_lib" );

print( "\nPatching changes.txt:\n" );

my $build = add_build_to_changes();

$sftp->put( "changes.txt", "$FTPDIR/changes.txt", copy_perms => 0 )
	or die "Can't put changes.txt. ".$sftp->error;

# generation changes.xml out of changes.txt	
system("cd .. && ./gradlew rss");

$sftp->put( "changes.xml", "$FTPDIR/changes.xml", copy_perms => 0 )
	or die "Can't put changes.xml. ".$sftp->error;	

if( $was_copy )
{
	system( "git commit *.properties changes.txt -m \"PUBLISH $build\" && git push origin master" ) == 0
		or die "Error: $!.\n";
}

sub add_build_to_changes
{
 	open FILE, "<update/update.properties" or die "Can't find build.number\n";
	my $build = undef;
	while (my $line = <FILE>)
	{	
    	if ($line =~ /number=(\d+)/)
		{
			$build = $1;
			last;
		}	
	}
	close FILE;	

	my $date = `TZ="UTC-5" date "+%d.%m.%Y %X"`;
	chomp($date);
	print("Build: $build; date: $date");

	my $changes = "";
	my $changes_started = undef;
	my $dates_started = undef;

	open FILE, "<changes.txt" or die "Can't open changes.txt\n";
	while (my $line = <FILE>)
	{
		$dates_started = $dates_started || ($line =~ /^\d{2}\.\d{2}\.\d{4}/);	
		if (!$dates_started && !$changes_started && ($changes_started = ($line =~ /^[FACАС]:/)))
		{
			$changes .= "$date $build\n";
		}
		$changes .= "$line";
	}
	close FILE;
	
    #print($changes);

	if ($changes_started) 
	{
		open FILE, ">changes.txt" or die "Can't write changes.txt\n";
		print FILE $changes;
		close FILE;
	}

	return $build;		
}

sub dir_sync
{
	my $local_dir = shift;
	
	opendir DIR, $local_dir
		or die "Can't open local dir $local_dir\n";

	foreach my $file ( readdir( DIR ) )
	{
		if( $file =~ /^\.+/ )
		{
			next;
		}
		
		my @local = get_name_build( $file );
		my $local_name = $local[0];
		my $local_build = $local[1];
		
		if( !$local_name || !$local_build )
		{
			next;
		}
		
		my $remote_data = $remote_data{$local_name};
		my @remote = undef;
		if( $remote_data )
		{
			@remote = @{ $remote_data };
		}
		
		#print( "DEBUG $file $remote[0]  $remote[1]\n" );

		if( !$remote_data || $remote[0] < $local_build )
		{
			print( "Put $local_dir/$file to $FTPDIR/$file.\n" );

			$sftp->put( "$local_dir/$file", "$FTPDIR/$file", copy_perms => 0 )
				or die "Can't put $local_dir/$file. ".$sftp->error;
=pod
			my $command = "scp $local_dir/$file ${SSH_USER}\@${SSH_HOST}:$FTPDIR";

			print( $command."\n" );

			system( $command ) == 0
				or die "Can't put $local_dir/$file. ".$sftp->error;
=cut				
 							
			if( $remote_data )
			{
				my $old_file = $remote[1];
			
				print( " Remove $old_file.\n" );
				$sftp->remove( "$FTPDIR/$old_file" )
					or die "Can't remove. ".$sftp->error;
			}
			print( "\n" );
			
=pod			
			if( $local_dir eq 'bgerp' )
			{
				system( "git commit bgerp/build.number -m publish" );
			}
			else
			{
				system( "git commit $local_dir/$local_dir.properties -m publish" );
			}
=cut			

			$was_copy = 1;
		}
	}

	closedir DIR;
}

sub get_name_build
{
	my $file = shift;

	my $name = "";
	my $build = "";
	
	if( $file =~ /(.+)_((?:\d+\.\d+)|(?:release-[a-zA-Z]+))_(\d+)\.zip/ )
	{
		$name = $1;
		$build = $3;
	}

	return ($name, $build);
}

$sftp->disconnect();
