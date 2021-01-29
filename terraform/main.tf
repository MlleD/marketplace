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
  access_key = "$AWS_ACCESS_KEY"
  secret_key = "$AWS_SECRET_ACCESS_KEY"
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
