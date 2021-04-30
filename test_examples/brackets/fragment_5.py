str = input()

res = []
for offset in range(len(input) // 2):
    res.extend([input[offset], '('])