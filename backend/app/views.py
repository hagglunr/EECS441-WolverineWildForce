from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
import os, time
from django.conf import settings
from django.core.files.storage import FileSystemStorage
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


@csrf_exempt
def postfloorplans(request):
    if request.method != 'POST':
        return HttpResponse(status=400)

    # loading form-encoded data
    building_name = request.POST.get("building_name")
    floor = request.POST.get("floor")

    if request.FILES.get("image"):
        content = request.FILES['image']
        filename = building_name+"_"+floor+".jpeg"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        imageurl = fs.url(filename)
    else:
        imageurl = None
        
    cursor = connection.cursor()
    cursor.execute('INSERT INTO floorplans (building_name, floor, imageurl) VALUES '
                   '(%s, %s, %s);', (building_name, floor, imageurl))

    return JsonResponse({})

def getfloorplans(request):
    if request.method != 'GET':
	    return HttpResponse(status=404)

    response = {}
    if 'building' not in request.GET or request.GET['building'] == '':
	    return  JsonResponse(response)

    cursor = connection.cursor()
    cursor.execute('SELECT * FROM floorplans WHERE building_name = (%s);', (request.GET['building'],))
    rows = cursor.fetchall()
    response[request.GET['building']] = rows
    return JsonResponse(response)
