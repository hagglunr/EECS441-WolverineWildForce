from django.http import JsonResponse, HttpResponse
# Create your views here.

def getNodes(request):
    if request.method != 'GET':
	return HttpResponse(status=404)
    response = {}
    if !request.GET.get('building', '').building:
	return  HttpResponse(status=404)
    
