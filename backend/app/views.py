from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.db import connection
import json
# Create your views here.

def getNodes(request):
    response = {}
    return  JsonResponse(response)
    if request.method != 'GET':
	    return HttpResponse(status=404)
    
    response = {}
    if 'building' not in request.GET or request.GET['building'] == '':
	    return  JsonResponse(response)

    cursor = connection.cursor()
    cursor.execute('SELECT * FROM nodes WHERE building_name = %s;', (request.GET['building']))
    rows = cursor.fetchall()

    response[request.GET['building']] = rows
    return JsonResponse(response)

def postNodes(request):
    if request.method != 'POST':
	    return HttpResponse(status=404)

    json_data = json.loads(request.body)
    building_name = json_data['building_name']
    id = json_data['id']
    type = json_data['type']
    name = json_data['name']
    floor = json_data['floor']
    neighbors = json_data['neighbors']
    long = json_data['coordinates'][0]
    lat = json_data['coordinates'][1]
    cursor = connection.cursor()
    cursor.execute('INSERT INTO nodes (building_name, name, id, type, floor, long, lat, neighbors) VALUES '
                   '(%s, %s, %s, %s, %s, %s, %s, %s);', (building_name, name, id, type, floor, long, lat, neighbors))
    return JsonResponse({})