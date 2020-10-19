class A:
    def __init__(self, filename: str = None, xml_str: str = None):
        self.py_ast_ = None
        self.filename = filename
        self.xml_str = xml_str

def foo(a, b):
    print(a)
    print(b)
    return a + b

foo()
