str = input()

res = []
for char in str[:len(str) // 2]:
    res.append(char)
    res.append('(')
