from flask import Flask, request, jsonify, abort
from flask_restful import Resource, Api, reqparse
import mysql.connector
from flaskext.mysql import MySQL
from mysql.connector import Error
import json
import os

# Flask api setup
app = Flask(__name__)
mysql = MySQL()
app.config['MYSQL_DATABASE_USER'] = os.getenv('SQL_USERNAME')
app.config['MYSQL_DATABASE_PASSWORD'] = os.getenv('SQL_PASSWORD')
app.config['MYSQL_DATABASE_DB'] = 'voter_data'
app.config['MYSQL_DATABASE_HOST'] = 'localhost'
api = Api(app)
mysql.init_app(app)

# current states availible 
states = ('ct')

class GeoClosest(Resource):
	def get(self, state, record_count):
			# parse and validate parameters
			parser = reqparse.RequestParser()
			parser.add_argument('twoParty', type=bool, default=False, required=False)
			parser.add_argument('maxRadius', type=int, default=5, required=False)
			parser.add_argument('latlng', type=str, required=True, help='latlng parameter is required')
			args = parser.parse_args()
			if not (0 <= args["maxRadius"] <= 10):
				abort(400, 'The value of the maxRadius parameter must be between 0 and 10')
			validate_state(state)
			# convert coordinates to floats
			try:
				lat, lng = [float(s) for s in args["latlng"].split(',', 1)]
			except ValueError:
				abort(400, 'latlng parameter formatted impropery. Should be formatted as lat,lng')
			# get data from sql
			try:
				procedure = "CALL sp_" + state + "_ClostestRecords(%s, %s, %s, %s, %s)"
				data = (lat, lng, args["maxRadius"], record_count, args["twoParty"])
				return {"records" : query_to_json(procedure % data)}
			except Error:
				abort(400, 'Failure on backend of server. Please try again later.')

class VoterInfo(Resource):
	def get(self, state, voter_id):
		validate_state(state)
		try:
			procedure = "CALL sp_" + state + "_VoterInfo('%s')"
			data = (voter_id)
			# parse record and return
			record = query_to_json(procedure % data)
			# clean election_history field later for a better json object
			if not record:
				abort(404, 'Voter ID record \"%s\" not found in %s' % (voter_id, state))
			return {"voter" : record[0]}
		except:
			abort(400, 'Failure on backend of server. Please try again later.')
			
		return

# class VoterHouseMembers(Resource):
# 	def get(self, state, voter_id):
# 		return

api.add_resource(GeoClosest, '/geo/<string:state>/closest/<int:record_count>')
api.add_resource(VoterInfo, '/voter/<string:state>/<string:voter_id>/info')
# api.add_resource(VoterHouseMembers, '/voter/<string:state>/<string:voter_id>/housemembers')

def validate_state(state):
	if state not in states:
		abort(400, ("The state shortcode \"%s\" is incorrect.  Please remember the "
		"only the state codes which are available are %s "
		"and must be passed as lowercase.") % (state, states))

def query_to_json(query):
	conn = mysql.connect()
	cursor = conn.cursor()
	cursor.execute(query)
	result = cursor.fetchall()
	rows = [dict(zip([key[0] for key in cursor.description], row)) for row in result]
	return rows

if __name__ == '__main__':
	app.run(debug=True)

# notes for later
# from flask import Flask
# from flask_restful import Api
# from myapi.resources.foo import Foo
# from myapi.resources.bar import Bar
# from myapi.resources.baz import Baz

# app = Flask(__name__)
# api = Api(app)

# api.add_resource(Foo, '/Foo', '/Foo/<string:id>')
# api.add_resource(Bar, '/Bar', '/Bar/<string:id>')
# api.add_resource(Baz, '/Baz', '/Baz/<string:id>')