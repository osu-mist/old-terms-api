import json
import requests
import urllib2
import ssl
from configuration_load import *


def good_request(url, access_token):
    url += "open"
    headers = {'Authorization': access_token}
    request = requests.get(url, headers=headers)
    return request.status_code

def term_code_results(url, access_token):
    url += "999999"
    headers = {'Authorization': access_token}
    request = requests.get(url, headers=headers)
    response = request.json()
    return response["data"]["attributes"]

def results_with_links(url, access_token):
    headers = {'Authorization': access_token}
    request = requests.get(url, headers=headers)
    response = request.json()
    return response["links"]

def unauth_request(url):
    request = requests.get(url)
    return request.status_code
    
def not_found_request(url, access_token):
    url += "123456789"
    headers = {'Authorization': access_token}
    request = requests.get(url, headers=headers)
    return request

def not_found_status_code(url, access_token):
    return not_found_request(url, access_token).status_code

def not_found_json(url, access_token):
    return not_found_request(url, access_token).json()

def response_time(url, access_token):
    headers = {'Authorization': access_token}
    request = requests.get(url, headers=headers)
    response_time = request.elapsed.total_seconds()
    
    print "API response time: ", response_time, " seconds"
    return response_time

def check_ssl(protocol, url, access_token):
    try:
        context = ssl.SSLContext(protocol)
        request = urllib2.Request(url, headers={"Authorization" : access_token})
        urllib2.urlopen(request, context=context)
    except (urllib2.URLError):
        return False
    else:
        return True