terraform {
  backend "s3" {
  		access_key = "AKIAZOZWHLOTZC7PB5GV"
  		secret_key = "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"
		bucket = "poca-tfstates-equipe-7"
    	key = "poca-2020"
    	region = "eu-west-3"
    	}
	}