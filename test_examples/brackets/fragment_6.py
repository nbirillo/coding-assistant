str = input()

middle = len(str) // 2

res = []

for offset, char in enumarate(str):
    if offset < middle:
        res.append(char)
        res.append('(')
