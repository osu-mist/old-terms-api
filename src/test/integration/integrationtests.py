import unittest
import sys
import json
from api_request import *
from configuration_load import *

class gateway_tests(unittest.TestCase):

	# Tests that a good request returns a 200
	def test_verbs(self):
		self.assertEqual(good_request(url, access_token, "get"), 200)
		self.assertEqual(good_request(url, access_token, "post"), 405)
		self.assertEqual(good_request(url, access_token, "put"), 405)
		self.assertEqual(good_request(url, access_token, "delete"), 405)

	def test_attributes(self):
		attributes = term_code_results(url, access_token)
		self.assertIsNotNone(attributes["code"])
		self.assertIsNotNone(attributes["description"])
		self.assertIsNotNone(attributes["startDate"])
		self.assertIsNotNone(attributes["endDate"])
		self.assertIsNotNone(attributes["financialAidYear"])
		self.assertIsNotNone(attributes["housingStartDate"])
		self.assertIsNotNone(attributes["housingEndDate"])

	# Tests that GET all terms contains correct links
	def test_links(self):
		links = results_with_links(url, access_token)
		self.assertIsNotNone(links["self"])
		self.assertIsNotNone(links["first"])
		self.assertIsNotNone(links["last"])
		self.assertIsNone(links["prev"])
		self.assertIsNotNone(links["next"])

	# Tests that a request with auth header returns a 401
	def test_unauth(self):
		self.assertEqual(unauth_request(url), 401)

	# Tests that a nonexistent term code returns a 404. This test will fail. See Jira ticket ECSOPS-61.
	def test_not_found(self):
		self.assertEqual(not_found_status_code(url, access_token), 404)

	# Tests that a 404 response contains correct JSON fields. This test will error. See Jira ticket ECSOPS-61.
	def test_not_found_results(self):
		response = not_found_json(url, access_token)
		self.assertIsNotNone(response["status"])
		self.assertIsNotNone(response["developerMessage"])
		self.assertIsNotNone(response["userMessage"])
		self.assertIsNotNone(response["code"])
		self.assertIsNotNone(response["details"])

	# Tests that API response time is less than a value
	def test_response_time(self):
		self.assertLess(response_time(url, access_token), 1.5)

    # Tests that a call using TLSv1.0 fails
    def test_tls_v1_0(self):
        self.assertFalse(check_ssl(ssl.PROTOCOL_TLSv1, url, access_token))

    # Tests that a call using TLSv1.1 fails
    def test_tls_v1_1(self):
        self.assertFalse(check_ssl(ssl.PROTOCOL_TLSv1_1, url, access_token))

    # Tests that a call using TLSv1.2 is successful
    def test_tls_v1_2(self):
        self.assertTrue(check_ssl(ssl.PROTOCOL_TLSv1_2, url, access_token))

	# Tests that a call using SSLv2 is unsuccessful
	def test_ssl_v2(self):
		try:
			# openssl can be compiled without SSLv2 support, in which case
			# the PROTOCOL_SSLv2 constant is not available
			ssl.PROTOCOL_SSLv2
		except AttributeError:
			self.skipTest('SSLv2 support not available')
		self.assertFalse(check_ssl(ssl.PROTOCOL_SSLv2, url, access_token))

	# Tests that a call using SSLv3 is unsuccessful
	def test_ssl_v3(self):
		try:
			# openssl can be compiled without SSLv3 support, in which case
			# the PROTOCOL_SSLv3 constant is not available
			ssl.PROTOCOL_SSLv3
		except AttributeError:
			self.skipTest('SSLv3 support not available')
		self.assertFalse(check_ssl(ssl.PROTOCOL_SSLv3, url, access_token))

if __name__ == '__main__':
	options_tpl = ('-i', 'config_path')
	del_list = []

	for i,config_path in enumerate(sys.argv):
		if config_path in options_tpl:
			del_list.append(i)
			del_list.append(i+1)

	del_list.reverse()

	for i in del_list:
		del sys.argv[i]

	url = get_endpoint_url(config_path)
	access_token = get_access_token(config_path)

	unittest.main()
