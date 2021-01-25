/*
The AWS user used by terraform is granted the AWS managed policy AdministratorAccess.
*/

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.7.0"
    }
  }
}

provider "aws" {
  access_key = "AKIAZOZWHLOTZC7PB5GV"
  secret_key = "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"
  region = "eu-west-3"  # Europe (Paris)
}

data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners = ["amazon"]

  filter {
    name = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}
