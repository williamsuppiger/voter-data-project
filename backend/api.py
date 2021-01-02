from flask import Flask, request
from flask_restful import Resource, Api, reqparse
import mysql.connector
from mysql.connector import Error
import os

# Flask api setup
app = Flask(__name__)
api = Api(app)

todos = {}

# mysql connect
try:
	connection = mysql.connector.connect(host='localhost',
													database='voter_data',
													user=os.getenv('SQL_USERNAME'),
													password=os.getenv('SQL_PASSWORD'))
except mysql.connector.Error as error:
	print("Failed to execute stored procedure: {}".format(error))


class TodoSimple(Resource):
	def get(self, todo_id):
		return {todo_id: todos[todo_id]}
		  #  cursor = connection.cursor()
		  #  cursor.callproc('get_laptop', [1, ])
		  #  # print results
		  #  print("Printing laptop details")
		  #  for result in cursor.stored_results():
		  # 	  print(result.fetchall())
		  #   cursor.close()

	def put(self, todo_id):
		todos[todo_id] = request.form['data']
		return {todo_id: todos[todo_id]}

class GeoClosest(Resource):
	def get(self, state, record_count):
			parser = reqparse.RequestParser()
			parser.add_argument('latlng', type=str, required=True, help='latlng parameter is required')
			parser.add_argument('twoParty', type=bool, default=False, required=False)
			args = parser.parse_args()


api.add_resource(GeoClosest, '/geo/<string:state>/closest/<int:record_count>')

if __name__ == '__main__':
	app.run(debug=True)


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