v1 = int(input())
v2 = int(input())
v3 = int(input())
if v1 > v2 and v1 > v3:
    print(v1)
elif v1 == v2 and v1 > v3:
    print(v1)
elif v2 > v1 and v2 > v3:
    print(v2)
elif v2 > v1 and v2 == v3:
    print(v2)
elif v3 > v1 and v3 > v2:
    print(v3)
elif v3 == v1 and v3 > v2:
    print(v1)
elif v1 == v2 and v2 == v3:
    print(v1)
