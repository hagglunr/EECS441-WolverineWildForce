from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
import json

# Create your views here.

def getnodes(request):
    if request.method != 'GET':
	    return HttpResponse(status=404)

    response = {}
    if 'building' not in request.GET or request.GET['building'] == '':
	    return  JsonResponse(response)
    cursor = connection.cursor()
    cursor.execute('SELECT * FROM nodes WHERE building_name = (%s);', (request.GET['building'],))
    rows = cursor.fetchall()
    response[request.GET['building']] = rows
    return JsonResponse(response)
    
def getbuildings(request):
    if request.method != 'GET':
        return HttpResponse(status=404)

    response = {}
    cursor = connection.cursor()
    cursor.execute('SELECT DISTINCT building_name FROM nodes;')
    rows = cursor.fetchall()
    response['buildings'] = rows
    return JsonResponse(response)

def getrooms(request):
    if request.method != 'GET':
        return HttpResponse(status=404)

    response = {}
    if 'building' not in request.GET or request.GET['building'] == '':
	    return  JsonResponse(response)

    cursor = connection.cursor()
    cursor.execute('SELECT name FROM nodes WHERE type = (%s) AND building_name = (%s);', ('Room', request.GET['building'],))
    rows = cursor.fetchall()
    response[request.GET['building']+' Rooms'] = rows
    return JsonResponse(response)


@csrf_exempt
def postnodes(request):
    if request.method != 'POST':
	    return HttpResponse(status=404)
    json_data = json.loads(request.body)
    building_name = json_data['building_name']
    name = json_data['name']
    id = json_data['id']
    type = json_data['type']
    floor = json_data['floor']
    long = json_data['coordinates'][0]
    lat = json_data['coordinates'][1]
    neighbors = json_data['neighbors']
    cursor = connection.cursor()
    cursor.execute('INSERT INTO nodes (building_name, name, id, type, floor, long, lat, neighbors) VALUES '
                   '(%s, %s, %s, %s, %s, %s, %s, %s);', (building_name, name, id, type, floor, long, lat, neighbors,))
    return JsonResponse({'status': 'success!'})

